package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.banish.CustomRunnable;
import me.crz.minecraft.kingdoms.perk.VillagePerk;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.vault.Levels;
import org.bukkit.Location;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import org.bukkit.entity.Player;

import me.crz.minecraft.kingdoms.db.DatabaseManager;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class VillageManager {
    private static VillageManager instance;
    private Map<String, Village> villages = new ConcurrentHashMap<>();
    private Map<IdObject, Village> villageIDs = new ConcurrentHashMap<>();

    private Map<String, VillagePerk> perks;
    private Map<String, VillagePerk> perkTags;

    public VillageManager() {
        super();
        loadPerks();
        for (Village village : DatabaseManager.getInstance().getAllVillages()) {
            villages.put(village.getName().toLowerCase(), village);
            villageIDs.put(village.getId(), village);
        }
    }

    public void loadPerks() {
        perks = new LinkedHashMap<>();
        perkTags = new LinkedHashMap<>();
        for (String perkName : ConfigManager.getInstance().getPerksConfig().getKeys(false)) {
            try {
                VillagePerk perk = new VillagePerk(ConfigManager.getInstance().getPerksConfig().getConfigurationSection(perkName));
                perks.put(perk.name().toLowerCase(), perk);
                perkTags.put(perk.getTag().toLowerCase(), perk);
            } catch (IllegalArgumentException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Config for Perk " + perkName + " was invalid! " + e.getMessage());
            }
        }
    }

    public static VillageManager getInstance() {
        if (instance == null) {
            instance = new VillageManager();
        }
        return instance;
    }

    public Collection<Village> getVillages() {
        return villages.values();
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
                }
            }
            for (Resident resident : village.getResidents()) {
                resident.setVillage(null);
            }
        });
    }


    public void isResBanished(OwnedLand land, Player p, CustomRunnable customRunnable) {
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
        Village village = resident.getVillage();
        resident.setVillage(null);
        long taxOwed = resident.getTaxOwed();
        if (taxOwed > 0) {
            resident.setTaxOwed(0);
            if (KingdomsPlugin.getInstance().econ.withdrawPlayer(resident.getPlayer(), taxOwed).transactionSuccess()) {
                Message.get("tax.removed").tell(resident,
                        "amount", String.valueOf(taxOwed),
                        "state", village.getState().getTag(),
                        "name", village.getName());
                village.addToBank(taxOwed);
                Utils.runAsync(() -> DatabaseManager.getInstance().log(village, resident, "tax", "cash", taxOwed));
            } else {
                village.addBanishedResident(resident);
                Message.get("tax.couldnt-pay").tell(resident,
                        "amount", String.valueOf(taxOwed),
                        "state", village.getState().getTag(),
                        "name", village.getName());
            }
        }
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveLand(lands);
            DatabaseManager.getInstance().saveResident(resident);
            DatabaseManager.getInstance().saveVillage(village);
        });
    }

    /**
     * Check whether or not a village can level up
     * @param village The village to check
     * @param res
     * @return Whether or not the village was leveled up
     */
    public boolean checkLevelUp(Village village, Resident res) {
        if (!village.canLevelUp()) {
            return false;
        }

        if (village.getLevel() == Levels.VILLAGE_TO_KINGDOM_LEVEL && village.getState() == VillageState.VILLAGE) {
            village.setState(VillageState.KINGDOM);
            village.setLevel(1);
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("levels.upgrade-settlement"), "state", village.getState().getTag());
        } else {
            village.setLevel(village.getLevel() + 1);
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("levels.upgrade"), "level", Integer.toString(village.getLevel()), "state", village.getState().getTag());
        }
        removeLevelRequirements(village, res);

        for (VillagePerk perk : getPerks()) {
            if (village.getState() == perk.getState() && village.getLevel() == perk.getLevel()) {
                if (perk.getUnlockCost().isEmpty()) {
                    MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("levels.perk-unlocked"), "perk", perk.getTag());
                    village.unlockPerk(perk);
                } else {
                    MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("levels.perk-available"), "perk", perk.getTag());
                }
            }
        }
        return true;
    }

    private void removeLevelRequirements(Village village, Resident res) {
        Map<String, Long> levelReqs = Levels.getLevelRequirements(village.getState(), village.getLevel());
        if (village.remove(levelReqs)) {
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(village);
                DatabaseManager.getInstance().logLevelup(village, res, levelReqs);
            });
        }
    }

    public VillagePerk getPerk(String name) {
        VillagePerk perk = perks.get(name.toLowerCase());
        if (perk != null) {
            perk = perkTags.get(name.toLowerCase());
        }
        return perk;
    }

    public Collection<VillagePerk> getPerks() {
        return perks.values();
    }

    public void changeName(Village village, String name) {
        villages.remove(village.getName().toLowerCase());
        village.setName(name);
        villages.put(village.getName().toLowerCase(), village);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
    }
}
