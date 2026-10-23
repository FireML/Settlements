package de.themoep.settlements.land;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigMessagesOld;
import de.themoep.settlements.utils.Pair;
import de.themoep.settlements.utils.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface LandAccessChecker extends ConfigMessagesOld, Listener {

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
                Pair<Boolean, String> rOwner = getCanBuild(user, land.getSettlement(), land.getSettlement().getUseState(use), "use");
                if (r == null || (rOwner != null && !rOwner.getLeft() && !land.isPlot())) {
                    r = rOwner;
                }
            }
            if (r != null) {
                r.setRight(StringUtils.parsePlaceholders(r.getRight(), "use", use.getTag()));
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
                Pair<Boolean, String> rOwner = getCanBuild(building, land.getSettlement(), land.getSettlement().getBuildState(), "build");
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
        if (land.isPlotOwner(building) || land.getSettlement().isChief(building.getUniqueId())) {
            return new Pair<>(true, "");
        }

        if (state == null) {
            return null;
        }

        if (!state.appliesTo(BuildState.AppliesTo.LAND)) {
            state = BuildState.PRIVATE;
        }

        if (!land.isPlot() && !state.appliesTo(BuildState.AppliesTo.get(land.getSettlement()))) {
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
                owner = land.getPlotOwnerResident();
                allowed |= owner.isFriend(building.getUniqueId());
            }
        }
        if (BuildState.OFFICERS.atLeast(state)) {
            allowed |= land.getSettlement().isOfficer(building);
        }
        if (BuildState.RESIDENTS.atLeast(state)) {
            allowed |= land.getSettlement().isMember(ResidentManager.getInstance().getResident(building));
        }
        if (BuildState.ALLY == state) {
            Resident resident = ResidentManager.getInstance().getResident(building);
            allowed |= land.getSettlement().isMember(resident) || land.getSettlement().isAlly(resident);
        }
        if (BuildState.PUBLIC.atLeast(state)) {
            allowed = true;
        }

        if (allowed) {
            return new Pair<>(true, "");
        }
        return new Pair<>(false, getMessage("state." + type + "." + land.getSettlement().getKey() + ".plot", state.getKey()).getReplacedText(
                "owner", owner != null ? owner.getName() : land.getPlotOwner() != null ? land.getPlotOwner().toString() : "-",
                "name", land.getSettlement().getName(),
                "state", land.getSettlement().getStateTag()
        ));
    }

    default Pair<Boolean, String> getCanBuild(Player building, Settlement settlement, BuildState state, String type) {
        if (settlement.isChief(building.getUniqueId())) {
            return new Pair<>(true, "");
        }

        if (state == null || !state.appliesTo(settlement.getBuildStateType())) {
            state = settlement.getDefaultBuildState();
        }
        boolean allowed;

        Resident resident = null;
        switch (state) {
            case OFFICERS -> allowed = settlement.isOfficer(building);
            case PRIVATE -> allowed = settlement.isMember(ResidentManager.getInstance().getResident(building));
            case RESIDENTS -> {
                resident = ResidentManager.getInstance().getResident(building);
                allowed = settlement.isMember(resident);
            }
            case ALLY -> {
                resident = ResidentManager.getInstance().getResident(building);
                allowed = settlement.isMember(resident) || settlement.isAlly(resident);
            }
            case PUBLIC -> allowed = true;
            default -> {
                return null;
            }
        }

        if (allowed && resident != null && settlement.useGuestRank() && settlement.getRank(resident) == settlement.getDefaultRank()) {
            return new Pair<>(false, getError("rules-not-accepted").getReplacedText(
                    "name", settlement.getName(),
                    "state", settlement.getStateTag()
            ));
        }

        if (allowed) {
            return new Pair<>(true, "");
        }
        return new Pair<>(false, getMessage("state." + type + "." + settlement.getKey() + ".landowner", state.getKey()).getReplacedText(
                "name", settlement.getName(),
                "state", settlement.getStateTag()
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
