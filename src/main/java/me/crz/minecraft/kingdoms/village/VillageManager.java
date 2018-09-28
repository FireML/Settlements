package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.banish.CustomRunnable;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Location;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import org.bukkit.entity.Player;

import me.crz.minecraft.kingdoms.db.DatabaseManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VillageManager {
    private static VillageManager instance;
    private Map<String, Village> villages = new HashMap<>();

    public VillageManager() {
        super();
        for (Village village : DatabaseManager.getInstance().getAllVillages()) {
            villages.put(village.getName().toLowerCase(), village);
        }
    }

    public static VillageManager getInstance() {
        if (instance == null) {
            instance = new VillageManager();
        }
        return instance;
    }

    public void registerNewVillage(final Village village) {
        villages.put(village.getName().toLowerCase(), village);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
    }

    public Village getVillageFromName(final String name) {
        if (name.equals("")) {
            return null;
        }
        return villages.get(name.toLowerCase());
    }

    public Village getVillageFromResident(final Resident res) {
        return this.getVillageFromName(res.getVillageName());
    }

    public Village getVillageFromPlayer(final Player player) {
        return this.getVillageFromResident(ResidentManager.getInstance().getResident(player));
    }

    public void deleteVillage(final Village village) {
        villages.remove(village.getName().toLowerCase());
        LandManager.getInstance().unclaimLand(village);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().deleteVillage(village);
            for (final String ally : village.getAllies()) {
                final Village al = this.getVillageFromName(ally);
                if (al != null) {
                    al.removeAlly(village);
                    DatabaseManager.getInstance().saveVillage(al);
                }
            }
        });
    }


    public void isResBanished(final Location to, final Player p, final CustomRunnable customRunnable) {
        final OwnedLand land = LandManager.getInstance().getLand(to);
        if (land != null) {
            final Village village = this.getVillageFromName(land.getOwnerVillage());
            if (village != null && village.isBanished(p.getUniqueId())) {
                customRunnable.run(land, p, village);
            }
        }
    }

    /**
     * Runs database operation!
     */
    public void saveAll() {
        DatabaseManager.getInstance().saveAllVillages(villages.values());
    }

    public void leaveVillage(Resident resident) {
        Collection<OwnedLand> lands = LandManager.getInstance().getOwnedPlot(resident.getUUID());
        for (OwnedLand land : lands) {
            LandManager.getInstance().setPlotOwner(land, null);
        }
        resident.setVillageName("");
        resident.setVillageRank(VillageRank.RESIDENT);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveLand(lands);
            DatabaseManager.getInstance().saveResident(resident);
        });
    }
}
