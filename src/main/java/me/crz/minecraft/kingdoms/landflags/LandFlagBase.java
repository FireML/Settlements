package me.crz.minecraft.kingdoms.landflags;

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

public class LandFlagBase extends Base {
    private TitleBuilder pvpMessage;

    public LandFlagBase() {
        super();
        (pvpMessage = new TitleBuilder()).withActionbar(this.replaceColourCodes("&c&lPvp isn't enabled here!"));
    }

    public Pair<Boolean, String> canUse(Player user, Use use, Location loc) {
        if (user.isOp()) {
            return new Pair<>(true, "");
        }
        if (!this.isLandClaimed(loc)) {
            return new Pair<>(true, "");
        }
        OwnedLand land = LandManager.getInstance().getLand(loc);
        Village village = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
        if (land.isPlot()) {
            PlotBuildState state = land.getUseState(use);
            if (land.isPlotOwner(user)) {
                return new Pair<>(true, "");
            }
            if (state.equals(PlotBuildState.PUBLIC)) {
                return new Pair<>(true, "");
            }
            if (state.equals(PlotBuildState.RESIDENTS)) {
                return this.getCanBuildResidents(user, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7This land is owned by &e" + land.getOwnerVillage() + "&7.");
            }
            if (state.equals(PlotBuildState.OFFICERS)) {
                return this.getCanBuildOfficer(user, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7This land is in &eofficer only &7mode.");
            }
            if (state.equals(PlotBuildState.ALLY)) {
                return this.getCanBuildAlly(user, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7You are not allied to &e" + land.getOwnerVillage() + "&7.");
            }
            if (state.equals(PlotBuildState.PRIVATE)) {
                return land.isPlotOwner(user) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7Only the &ePlot owner&7 can interact here.");
            }
            if (state.equals(PlotBuildState.FRIENDS)) {
                return this.getCanBuildFriends(user, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7Only the &ePlot owner's friends&7 can interact here.");
            }
        } else {
            BuildState state2 = village.getUseState(use);
            if (state2.equals(BuildState.RESIDENTS)) {
                return this.getCanBuildResidents(user, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7This land is owned by &e" + land.getOwnerVillage() + "&7.");
            }
            if (state2.equals(BuildState.OFFICERS)) {
                return this.getCanBuildOfficer(user, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7This land is in &eofficer only &7mode.");
            }
            if (state2.equals(BuildState.ALLY)) {
                return this.getCanBuildAlly(user, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7You are not allied to &e" + land.getOwnerVillage() + "&7.");
            }
            if (state2.equals(BuildState.PUBLIC)) {
                return new Pair<>(true, "");
            }
        }
        return new Pair<>(true, "&cUnknown");
    }

    public Pair<Boolean, String> getCanBuildAll(Player building, Location loc) {
        if (building.isOp()) {
            return new Pair<>(true, "");
        }
        if (!this.isLandClaimed(loc)) {
            return new Pair<>(true, "");
        }
        OwnedLand land = LandManager.getInstance().getLand(loc);
        if (land.isPlot()) {
            if (land.isPlotOwner(building)) {
                return new Pair<>(true, "");
            }
            if (land.getBuildState().equals(PlotBuildState.PUBLIC)) {
                return new Pair<>(true, "");
            }
            if (land.getBuildState().equals(PlotBuildState.RESIDENTS)) {
                return this.getCanBuildResidents(building, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7This land is owned by &e" + land.getOwnerVillage() + "&7.");
            }
            if (land.getBuildState().equals(PlotBuildState.OFFICERS)) {
                return this.getCanBuildOfficer(building, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7This land is in &eofficer only &7mode.");
            }
            if (land.getBuildState().equals(PlotBuildState.ALLY)) {
                return this.getCanBuildAlly(building, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7You are not allied to &e" + land.getOwnerVillage() + "&7.");
            }
            if (land.getBuildState().equals(PlotBuildState.PRIVATE)) {
                return land.isPlotOwner(building) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7Only the &ePlot owner&7 can build here.");
            }
            if (land.getBuildState().equals(PlotBuildState.FRIENDS)) {
                return this.getCanBuildFriends(building, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7Only the &ePlot owner's friends&7 can build here.");
            }
        }
        Village village = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
        if (village.getBuildState().equals(BuildState.RESIDENTS)) {
            return this.getCanBuildResidents(building, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7This land is owned by &e" + land.getOwnerVillage() + "&7.");
        }
        if (village.getBuildState().equals(BuildState.OFFICERS)) {
            return this.getCanBuildOfficer(building, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7This land is in &eofficer only &7mode.");
        }
        if (village.getBuildState().equals(BuildState.ALLY)) {
            return this.getCanBuildAlly(building, land) ? new Pair<>(true, "") : new Pair<>(false, "&e&lALERT &r&7You are not allied to &e" + land.getOwnerVillage() + "&7.");
        }
        if (village.getBuildState().equals(BuildState.PUBLIC)) {
            return new Pair<>(true, "");
        }
        return new Pair<>(true, "&cUnknown");
    }

    private boolean getCanBuildFriends(Player building, OwnedLand land) {
        if (building.isOp()) {
            return true;
        }
        if (land == null) {
            return true;
        }
        Resident res = ResidentManager.getInstance().getResident(building);
        Resident owner = ResidentManager.getInstance().getResident(land.getPlotOwner());
        Village ownerVillage = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
        return ownerVillage.getChief().equals(building.getUniqueId()) || owner.getFriends().contains(res.getUUID());
    }

    public boolean getCanBuildResidents(Player building, OwnedLand land) {
        if (building.isOp()) {
            return true;
        }
        if (land == null) {
            return true;
        }
        Resident res = ResidentManager.getInstance().getResident(building);
        if (!res.hasVillage()) {
            return false;
        }
        Village ownerVillage = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
        return ownerVillage.getChief().equals(building.getUniqueId()) || res.getVillageName().equals(land.getOwnerVillage());
    }

    public boolean getCanBuildOfficer(Player building, OwnedLand land) {
        if (building.isOp()) {
            return true;
        }
        if (land == null) {
            return true;
        }
        Resident res = ResidentManager.getInstance().getResident(building);
        if (!res.hasVillage()) {
            return false;
        }
        Village ownerVillage = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
        return ownerVillage.getChief().equals(building.getUniqueId()) || (res.getVillageName().equals(land.getOwnerVillage()) && (res.getVillageRank().equals(VillageRank.BUILDER) || res.getVillageRank().equals(VillageRank.ELDER)));
    }

    public boolean getCanBuildAlly(Player building, OwnedLand land) {
        if (building.isOp()) {
            return true;
        }
        if (land == null) {
            return true;
        }
        Resident res = ResidentManager.getInstance().getResident(building);
        if (!res.hasVillage()) {
            return false;
        }
        Village ownerVillage = VillageManager.getInstance().getVillageFromName(land.getOwnerVillage());
        return ownerVillage.getChief().equals(building.getUniqueId()) || res.getVillageName().equals(land.getOwnerVillage()) || ownerVillage.isAlly(res.getVillageName());
    }

    public boolean isLandClaimed(Location loc) {
        return LandManager.getInstance().isLandClaimed(loc);
    }

    public boolean isKingdom(Village village) {
        return village.getState().equals(VillageState.KINGDOM);
    }

    public void sendPvPNotEnabledMessage(Player p) {
        pvpMessage.send(p);
    }

    public void sendPvPNotEnabledMessage(Entity e) {
        if (e instanceof Player) {
            pvpMessage.send((Player) e);
        }
    }

    public String replaceColourCodes(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
