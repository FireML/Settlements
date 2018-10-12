package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.banish.CustomRunnable;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Utils;
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
}
