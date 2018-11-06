package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.BuildState;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.utils.TitleBuilder;
import me.crz.minecraft.kingdoms.Base;

import java.util.UUID;

public interface LandFlagBase extends ConfigMessages {

    default Pair<Boolean, String> canUse(Player user, Use use, Location loc) {
        if (user.hasPermission("kingdoms.staff")) {
            return new Pair<>(true, "");
        }
        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(loc.getWorld());
        if (empire != null) {
            return new Pair<>(empire.canAccess(user), "&cEmpire world");
        }
        OwnedLand land = LandManager.getInstance().getLand(loc);
        if (land != null) {
            Pair<Boolean, String> r = getCanBuild(user, land, land.getUseState(use), "use");
            if (r == null) {
                r = getCanBuild(user, land.getOwnerVillage(), land.getOwnerVillage().getUseState(use), "use");
            }
            if (r != null) {
                return r;
            }
        }
        return new Pair<>(true, "&cUnknown");
    }

    default Pair<Boolean, String> getCanBuildAll(Player building, Location loc) {
        if (building.hasPermission("kingdoms.staff")) {
            return new Pair<>(true, "");
        }
        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(loc.getWorld());
        if (empire != null) {
            return new Pair<>(empire.canAccess(building), "&cEmpire world");
        }
        OwnedLand land = LandManager.getInstance().getLand(loc);
        if (land != null) {
            Pair<Boolean, String> r = getCanBuild(building, land, land.getBuildState(), "build");
            if (r == null) {
                r = getCanBuild(building, land.getOwnerVillage(), land.getOwnerVillage().getBuildState(), "build");
            }
            if (r != null) {
                return r;
            }
        }
        return new Pair<>(true, "&cUnknown");
    }

    default Pair<Boolean, String> getCanBuild(Player building, OwnedLand land, PlotBuildState state, String type) {
        if (!land.isPlot()) {
            return null;
        }
        if (land.isPlotOwner(building) || land.getOwnerVillage().isChief(building)) {
            return new Pair<>(true, "");
        }

        Resident res;
        Resident owner = null;
        boolean allowed = false;
        switch (state) {
            case PUBLIC:
                allowed = true;
                break;
            case FRIENDS:
                owner = ResidentManager.getInstance().getResident(land.getPlotOwner());
                allowed = owner.getFriends().contains(building.getUniqueId());
                break;
            case ALLY:
                res = ResidentManager.getInstance().getResident(building);
                allowed = res.hasVillage() && land.getOwnerVillage().isAlly(res.getVillage());
                break;
            case TRUSTED:
                res = ResidentManager.getInstance().getResident(building);
                allowed = res.hasVillage() && land.getOwnerVillage().isTrusted(res.getVillage());
                break;
            case OFFICERS:
                res = ResidentManager.getInstance().getResident(building);
                allowed = land.getOwnerVillage().equals(res.getVillage()) && res.getVillageRank().atLeast(VillageRank.BUILDER);
                break;
            case RESIDENTS:
                res = ResidentManager.getInstance().getResident(building);
                allowed = land.getOwnerVillage().equals(res.getVillage());
                break;
            case PRIVATE:
                allowed = land.isPlotOwner(building);
                break;
            default:
                return null;
        }
        if (allowed) {
            return new Pair<>(true, "");
        }
        return new Pair<>(false, getMessage("state." + type + "." + state.getKey()).getReplacedText(
                "owner", owner != null ? owner.getName() : land.getPlotOwner().toString(),
                "name", land.getOwnerVillage().getName(),
                "state", land.getOwnerVillage().getState().getTag()
        ));
    }

    default Pair<Boolean, String> getCanBuild(Player building, Village village, BuildState state, String type) {
        if (village.isChief(building)) {
            return new Pair<>(true, "");
        }

        Resident res;
        boolean allowed = false;
        switch (state) {
            case PUBLIC:
                return new Pair<>(true, "");
            case TRUSTED:
                res = ResidentManager.getInstance().getResident(building);
                allowed = res.hasVillage() && village.isTrusted(res.getVillage());
                break;
            case ALLY:
                res = ResidentManager.getInstance().getResident(building);
                allowed = res.hasVillage() && village.isAlly(res.getVillage());
                break;
            case OFFICERS:
                res = ResidentManager.getInstance().getResident(building);
                allowed = village.equals(res.getVillage()) && res.getVillageRank().atLeast(VillageRank.BUILDER);
                break;
            case RESIDENTS:
                res = ResidentManager.getInstance().getResident(building);
                allowed = village.equals(res.getVillage());
                break;
            default:
                return null;
        }
        if (allowed) {
            return new Pair<>(true, "");
        }
        return new Pair<>(false, getMessage("state." + type + "." + state.getKey()).getReplacedText(
                "name", village.getName(),
                "state", village.getState().getTag()
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
