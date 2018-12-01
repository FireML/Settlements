package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.village.Resident;

import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface LandFlagBase extends ConfigMessages {

    default Pair<Boolean, String> canUse(Player user, Use use, Location loc) {
        if (user.hasPermission("kingdoms.bypass.use")) {
            return new Pair<>(true, "");
        }
        OwnedLand land = LandManager.getInstance().getLand(loc);
        if (land != null) {
            Pair<Boolean, String> r = getCanBuild(user, land, land.getUseState(use), "use");
            if (r == null || (!land.isPlot() && !r.getLeft())) {
                Pair<Boolean, String> rOwner = getCanBuild(user, land.getLandOwner(), land.getLandOwner().getUseState(use), "use");
                if (r == null || (rOwner != null && !rOwner.getLeft() && !land.isPlot())) {
                    r = rOwner;
                }
            }
            if (r != null) {
                return r;
            }
        }
        return new Pair<>(true, "&cUnknown");
    }

    default Pair<Boolean, String> getCanBuildAll(Player building, Location loc) {
        if (building.hasPermission("kingdoms.bypass.build")) {
            return new Pair<>(true, "");
        }
        OwnedLand land = LandManager.getInstance().getLand(loc);
        if (land != null) {
            Pair<Boolean, String> r = getCanBuild(building, land, land.getBuildState(), "build");
            if (r == null || (!land.isPlot() && !r.getLeft())) {
                Pair<Boolean, String> rOwner = getCanBuild(building, land.getLandOwner(), land.getLandOwner().getBuildState(), "build");
                if (r == null || (rOwner != null && !rOwner.getLeft() && !land.isPlot())) {
                    r = rOwner;
                }
            }
            if (r != null) {
                return r;
            }
        }
        return new Pair<>(true, "&cUnknown");
    }

    default Pair<Boolean, String> getCanBuild(Player building, OwnedLand land, BuildState state, String type) {
        if (land.isPlotOwner(building) || land.getLandOwner().isChief(building.getUniqueId())) {
            return new Pair<>(true, "");
        }
        if (!state.appliesTo(BuildState.AppliesTo.LAND)) {
            state = BuildState.PRIVATE;
        }

        Resident owner = null;
        boolean allowed;
        if (state.atLeast(BuildState.PRIVATE)) {
            allowed = land.isPlotOwner(building);

        } else if (state.atLeast(BuildState.FRIENDS)) {
            owner = ResidentManager.getInstance().getResident(land.getPlotOwner());
            allowed = owner.getFriends().contains(building.getUniqueId());

        } else if (state.atLeast(BuildState.OFFICERS)) {
            allowed = land.getLandOwner().isOfficer(building);

        } else if (state.atLeast(BuildState.RESIDENTS)) {
            allowed = land.getLandOwner().isMember(ResidentManager.getInstance().getResident(building));

        } else if (state.atLeast(BuildState.ALLY)) {
            Resident resident = ResidentManager.getInstance().getResident(building);
            allowed = land.getLandOwner().equals(resident.getVillage()) || land.getLandOwner().isAlly(resident);

        } else if (state.atLeast(BuildState.TRUSTED)) {
            Resident resident = ResidentManager.getInstance().getResident(building);
            allowed = land.getLandOwner().equals(resident.getVillage()) || land.getLandOwner().isTrusted(resident);

        } else if (state.atLeast(BuildState.PUBLIC)) {
            allowed = true;

        } else {
            return null;
        }

        if (allowed) {
            return new Pair<>(true, "");
        }
        return new Pair<>(false, getMessage("state." + type + "." + land.getClass().getSimpleName().toLowerCase() + ".plot", state.getKey()).getReplacedText(
                "owner", owner != null ? owner.getName() : land.getPlotOwner() != null ? land.getPlotOwner().toString() : "-",
                "name", land.getLandOwner().getName(),
                "state", land.getLandOwner().getStateTag()
        ));
    }

    default Pair<Boolean, String> getCanBuild(Player building, LandOwner landOwner, BuildState state, String type) {
        if (landOwner.isChief(building.getUniqueId())) {
            return new Pair<>(true, "");
        }

        if (!state.appliesTo(landOwner.getBuildStateType())) {
            state = landOwner.getDefaultBuildState();
        }
        boolean allowed;

        Resident resident;
        switch (state) {
            case OFFICERS:
                allowed = landOwner.isOfficer(building);
                break;
            case PRIVATE:
                resident = ResidentManager.getInstance().getResident(building);
                if (landOwner instanceof Empire) {
                    allowed = ((Empire) landOwner).getCapital().isMember(resident);
                } else {
                    allowed = landOwner.isMember(resident);
                }
                break;
            case RESIDENTS:
                allowed = landOwner.equals(VillageManager.getInstance().getVillageFromPlayer(building));
                break;
            case ALLY:
                resident = ResidentManager.getInstance().getResident(building);
                allowed = landOwner.equals(resident.getVillage()) || landOwner.isAlly(resident);
                break;
            case TRUSTED:
                resident = ResidentManager.getInstance().getResident(building);
                allowed = landOwner.equals(resident.getVillage()) || landOwner.isTrusted(resident);
                break;
            case PUBLIC:
                allowed = true;
                break;
            default:
                return null;
        }

        if (allowed) {
            return new Pair<>(true, "");
        }
        return new Pair<>(false, getMessage("state." + type + "." + landOwner.getClass().getSimpleName().toLowerCase() + ".landowner", state.getKey()).getReplacedText(
                "name", landOwner.getName(),
                "state", landOwner.getStateTag()
        ));
    }

    @Override
    default String type() {
        return "";
    }

    @Override
    default String name() {
        return "protection";
    }

    default boolean isLandClaimed(Location location) {
        return LandManager.getInstance().isLandClaimed(location);
    }
}
