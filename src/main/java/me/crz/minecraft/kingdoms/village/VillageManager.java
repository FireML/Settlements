package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.banish.CustomRunnable;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.vault.Levels;
import org.bukkit.Location;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import org.bukkit.entity.Player;

import me.crz.minecraft.kingdoms.db.DatabaseManager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class VillageManager {
    private static VillageManager instance;
    private Map<String, Village> villages = new ConcurrentHashMap<>();
    private Map<IdObject, Village> villageIDs = new ConcurrentHashMap<>();

    public VillageManager() {
        super();
        for (Village village : DatabaseManager.getInstance().getAllVillages()) {
            villages.put(village.getName().toLowerCase(), village);
            villageIDs.put(village.getId(), village);
        }
    }

    public static VillageManager getInstance() {
        if (instance == null) {
            instance = new VillageManager();
        }
        return instance;
    }

    public void registerNewVillage(Village village) {
        if (!village.getId().isNull()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving " + village.getName() + " to the database! It already had an ID defined?");
            return;
        }
        villages.put(village.getName().toLowerCase(), village);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveVillage(village);
            if (!village.getId().isNull()) {
                villageIDs.put(village.getId(), village);
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving " + village.getName() + " to the database! No ID was returned.");
            }
        });
    }

    public Village getVillageAt(Location location) {
        OwnedLand ownedLand = LandManager.getInstance().getLand(location);
        return ownedLand != null ? ownedLand.getOwnerVillage() : null;
    }

    public Village getVillageFromId(IdObject id) {
        if (id == null) {
            return null;
        }
        return villageIDs.get(id);
    }

    public Village getVillageFromName(String name) {
        if (name.equals("")) {
            return null;
        }
        return villages.get(name.toLowerCase());
    }

    public Village getVillageFromResident(Resident res) {
        return res != null ? res.getVillage() : null;
    }

    public Village getVillageFromPlayer(Player player) {
        return this.getVillageFromResident(ResidentManager.getInstance().getResident(player));
    }

    public void deleteVillage(Village village) {
        villages.remove(village.getName().toLowerCase());
        LandManager.getInstance().unclaimLand(village);
        MessageUtils.broadcast(Message.get("broadcast.village-deletion"),
                "name", village.getName(),
                "state", village.getState().getTag());
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().deleteVillage(village);
            for (IdObject ally : village.getAllies()) {
                Village al = this.getVillageFromId(ally);
                if (al != null) {
                    al.removeAlly(village);
                    DatabaseManager.getInstance().saveVillage(al);
                }
            }
        });
    }


    public void isResBanished(Location to, Player p, CustomRunnable customRunnable) {
        OwnedLand land = LandManager.getInstance().getLand(to);
        if (land != null) {
            Village village = land.getOwnerVillage();
            if (village != null && village.isBanished(p.getUniqueId())) {
                customRunnable.run(land, p, village);
            }
        }
    }

    /**
     * Runs database operation!
     */
    public void saveAll() {
        DatabaseManager.getInstance().saveVillages(villages.values());
    }

    public void leaveVillage(Resident resident) {
        Collection<OwnedLand> lands = LandManager.getInstance().getOwnedPlot(resident.getUUID());
        for (OwnedLand land : lands) {
            LandManager.getInstance().setPlotOwner(land, null);
        }
        resident.setVillage(null);
        resident.setVillageRank(VillageRank.RESIDENT);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveLand(lands);
            DatabaseManager.getInstance().saveResident(resident);
        });
    }

    /**
     * Check the upkeep for all registered villages
     */
    public void doUpkeep() {
        for (Village village : villages.values()) {
            village.doUpkeep();
        }
    }

    /**
     * Check whether or not a village can level up
     * @param village The village to check
     * @return Whether or not the village was leveled up
     */
    public boolean checkLevelUp(Village village) {
        if (!village.canLevelUp()) {
            return false;
        }

        if (village.getLevel() == Levels.VILLAGE_TO_KINGDOM_LEVEL && village.getState() == VillageState.VILLAGE) {
            village.setState(VillageState.KINGDOM);
            village.setLevel(1);
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("levels.upgrade-settlement"), "state", village.getState().getTag());
        } else {
            village.setLevel(village.getLevel() + 1);
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("levels.upgrade"), "level", Integer.toString(village.getLevel()));
        }
        removeLevelRequirements(village);

        // TODO: This should be a config and new shop system:
        if (village.getState() == VillageState.KINGDOM) {
            String unlock;
            switch (village.getLevel()) {
                case 4:
                    unlock = "Teleporter";
                    break;
                case 5:
                    unlock = "Regen 1 Buff";
                    break;
                case 6:
                    unlock = "Speed 1 Buff";
                    break;
                case 7:
                    unlock = "Regen 2 Buff";
                    break;
                case 8:
                    unlock = "Haste 1 Buff";
                    break;
                case 9:
                    unlock = "Haste 2 Buff";
                    break;
                case 10:
                    unlock = "World";
                    break;
                default:
                    return true;
            }
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("levels.upgrade-unlock"), "unlock", unlock);
        }
        return true;
    }

    private void removeLevelRequirements(Village village) {
        Map<String, Integer> levelReqs = Levels.getLevelMapFromInt(village.getLevel(), village.getState());
        if (levelReqs == null || levelReqs.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Integer> entry : levelReqs.entrySet()) {
            if (village.getVault().containsKey(entry.getKey())) {
                village.getVault().put(entry.getKey(), village.getVault().get(entry.getKey()) - levelReqs.get(entry.getKey()));
            }
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
    }

}
