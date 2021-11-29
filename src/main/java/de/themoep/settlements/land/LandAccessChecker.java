package de.themoep.settlements.land;

import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Pair;
import de.themoep.settlements.settlement.SettlementManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface LandAccessChecker extends ConfigMessages, Listener {

    default Pair<Boolean, String> canUse(Player user, Use use, Location loc) {
        UseFlag flag = UseFlagManager.getInstance().get(use);
        if (flag == null) {
            return new Pair<>(true, "");
        }
        if (user.hasPermission("settlements.bypass.use")) {
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
                r.setRight(MessageUtils.replacePlaceholders(r.getRight(), "use", use.getTag()));
                return r;
            }
        }
        return new Pair<>(true, "&cUnknown");
    }

    default Pair<Boolean, String> getCanBuildAll(Player building, Location loc) {
        if (building.hasPermission("settlements.bypass.build")) {
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

        if (state == null) {
            return null;
        }

        if (!state.appliesTo(BuildState.AppliesTo.LAND)) {
            state = BuildState.PRIVATE;
        }

        if (!land.isPlot() && !state.appliesTo(BuildState.AppliesTo.get(land.getLandOwner()))) {
            return null;
        }

        Resident owner = null;
        boolean allowed = false;
        if (BuildState.PRIVATE.atLeast(state)) {
            if (land.isPlot()) {
                allowed = land.isPlotOwner(building);
            }
        }
        if (BuildState.MEMBERS.atLeast(state)) {
            if (land.isPlot()) {
                allowed |= land.getPlotMembers().contains(building.getUniqueId());
            }
        }
        if (BuildState.FRIENDS.atLeast(state)) {
            if (land.isPlot()) {
                owner = ResidentManager.getInstance().getResident(land.getPlotOwner());
                allowed |= owner.getFriends().contains(building.getUniqueId());
            }
        }
        if (BuildState.OFFICERS.atLeast(state)) {
            allowed |= land.getLandOwner().isOfficer(building);
        }
        if (BuildState.RESIDENTS.atLeast(state)) {
            allowed |= land.getLandOwner().isMember(ResidentManager.getInstance().getResident(building));
        }
        if (BuildState.ALLY == state) {
            Resident resident = ResidentManager.getInstance().getResident(building);
            allowed |= land.getLandOwner().equals(resident.getSettlement()) || land.getLandOwner().isAlly(resident);
        }
        if (BuildState.TRUSTED == state) {
            Resident resident = ResidentManager.getInstance().getResident(building);
            allowed |= land.getLandOwner().equals(resident.getSettlement()) || land.getLandOwner().isTrusted(resident);
        }
        if (BuildState.PUBLIC.atLeast(state)) {
            allowed = true;
        }

        if (allowed) {
            return new Pair<>(true, "");
        }
        return new Pair<>(false, getMessage("state." + type + "." + land.getLandOwner().getKey() + ".plot", state.getKey()).getReplacedText(
                "owner", owner != null ? owner.getName() : land.getPlotOwner() != null ? land.getPlotOwner().toString() : "-",
                "name", land.getLandOwner().getName(),
                "state", land.getLandOwner().getStateTag()
        ));
    }

    default Pair<Boolean, String> getCanBuild(Player building, LandOwner landOwner, BuildState state, String type) {
        if (landOwner.isChief(building.getUniqueId())) {
            return new Pair<>(true, "");
        }

        if (state == null || !state.appliesTo(landOwner.getBuildStateType())) {
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
                allowed = landOwner.equals(SettlementManager.getInstance().getFromPlayer(building));
                break;
            case ALLY:
                resident = ResidentManager.getInstance().getResident(building);
                allowed = landOwner.equals(resident.getSettlement()) || landOwner.isAlly(resident);
                break;
            case TRUSTED:
                resident = ResidentManager.getInstance().getResident(building);
                allowed = landOwner.equals(resident.getSettlement()) || landOwner.isTrusted(resident);
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
        return new Pair<>(false, getMessage("state." + type + "." + landOwner.getKey() + ".landowner", state.getKey()).getReplacedText(
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
