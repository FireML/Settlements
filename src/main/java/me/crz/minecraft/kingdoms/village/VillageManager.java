package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.lock.LockProvider;
import me.crz.minecraft.kingdoms.perk.LevelPerk;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.vault.Levels;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class VillageManager {
    private static VillageManager instance;
    private Map<String, Village> villages = new ConcurrentHashMap<>();
    private Map<IdObject, Village> villageIDs = new ConcurrentHashMap<>();

    private Map<String, LevelPerk> perks;
    private Map<String, LevelPerk> perkTags;

    private VillageManager() {
        loadPerks();
        for (Village village : DatabaseManager.getInstance().getAllVillages()) {
            villages.put(village.getName().replace(' ', '_').toLowerCase(), village);
            villageIDs.put(village.getId(), village);
            if (village.isAbandoned()) {
                long timeAbandoned = System.currentTimeMillis() - village.getCreated();
                long abandonTime = Utils.getAbandonTime(village.getStateKey());
                long delay = timeAbandoned > abandonTime * 60 * 1000 ? 0 : abandonTime * 60 * 1000 - timeAbandoned;
                KingdomsPlugin.logDebug(village.getStateTag() + " " + village.getName() + " will be removed after " + abandonTime + " minutes in " + delay + "!");
                Utils.runLater(() -> {
                    KingdomsPlugin.getInstance().getLogger().log(Level.INFO, village.getStateTag() + " " + village.getName() + " was removed because it has been abandoned for " + abandonTime + " minutes!");
                    deleteVillage(village);
                }, delay, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void loadPerks() {
        perks = new LinkedHashMap<>();
        perkTags = new LinkedHashMap<>();
        for (String perkName : ConfigManager.getInstance().getPerksConfig().getKeys(false)) {
            try {
                LevelPerk perk = new LevelPerk(ConfigManager.getInstance().getPerksConfig().getConfigurationSection(perkName));
                perks.put(perk.name().toLowerCase(), perk);
                perkTags.put(perk.getTag().toLowerCase(), perk);
            } catch (IllegalArgumentException e) {
                KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Config for Perk " + perkName + " was invalid! " + e.getMessage());
            }
        }
    }

    synchronized public static VillageManager getInstance() {
        if (instance == null) {
            instance = new VillageManager();
        }
        return instance;
    }

    public Collection<Village> getVillages() {
        return villageIDs.values();
    }

    public void registerNewVillage(Resident chief, Village village) {
        if (!village.getId().isNull()) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving " + village.getName() + " to the database! It already had an ID defined?");
            chief.tell(ChatColor.RED + "Error while saving " + village.getName() + " to the database! It already had an ID defined?");
            return;
        }
        village.setBank(Math.min(Utils.getVillageStartBalance(), Utils.getVillageCreationCost()));
        villages.put(village.getName().replace(' ', '_').toLowerCase(), village);
        chief.setVillage(village);
        chief.setVillageRank(VillageRank.getChief());
        chief.addChannel(ChatChannel.SETTLEMENT);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveVillage(village);
            if (!village.getId().isNull()) {
                villageIDs.put(village.getId(), village);
                DatabaseManager.getInstance().saveResident(chief);
            } else {
                KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving " + village.getName() + " to the database! No ID was returned.");
                chief.tell(ChatColor.RED + "Error while saving " + village.getName() + " to the database! No ID was returned.");
            }
        });
    }

    public Village getVillageAt(Location location) {
        OwnedLand ownedLand = LandManager.getInstance().getLand(location);
        return ownedLand != null && ownedLand.getLandOwner() instanceof Village ? (Village) ownedLand.getLandOwner() : null;
    }

    public Village getVillageFromId(IdObject id) {
        if (id == null || id.isNull()) {
            return null;
        }
        return villageIDs.get(id);
    }

    public Village getVillageFromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return villages.get(name.replace(' ', '_').toLowerCase());
    }

    public Village getVillageFromResident(Resident res) {
        return res != null ? res.getVillage() : null;
    }

    public Village getVillageFromPlayer(Player player) {
        return this.getVillageFromResident(ResidentManager.getInstance().getResident(player));
    }

    public void deleteVillage(Village village) {
        villages.remove(village.getName().replace(' ', '_').toLowerCase());
        villageIDs.remove(village.getId());
        if (village.hasEmpire()) {
            village.getEmpire().getMemberIds().remove(village.getId());
        }
        MessageUtils.broadcast(Message.get("broadcast.village-deletion"),
                "name", village.getName(),
                "state", village.getState().getTag());

        List<Resident> residents = village.getResidents();
        for (Resident resident : residents) {
            removeResidentFromVillage(resident);
        }
        LandManager.getInstance().unclaimLand(village);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().deleteVillage(village);
            DatabaseManager.getInstance().saveResident(residents);
            for (LandOwner ally : village.getAllies()) {
                ally.removeAlly(village);
            }
        });
    }

    void abandonVillage(Village village) {
        Message.get("broadcast.village-abandoned").broadcast("state", village.getStateTag(), "name", village.getName());

        if (village.hasEmpire()) {
            village.getEmpire().getMemberIds().remove(village.getId());
        }

        List<Resident> residents = village.getResidents();
        for (Resident resident : residents) {
            removeResidentFromVillage(resident);
        }
        village.getResidentIds().clear();
        Collection<LandOwner> allies = village.getAllies();
        village.getAlliedEmpireIds().clear();
        village.getAlliedVillageIds().clear();
        for (LandOwner ally : allies) {
            ally.removeAlly(village);
        }
        village.setAccess(LandAccess.PRIVATE);
        village.setBuildState(BuildState.PUBLIC);
        for (Use u : Use.values()) {
            village.setUseState(u, BuildState.PUBLIC);
        }
        village.setMode(PlotMode.pvp, true);
        village.setMode(PlotMode.fireSpread, true);
        village.setMode(PlotMode.explosions, true);
        village.setMode(PlotMode.buff, false);
        village.setMode(PlotMode.mobSpawning, true);

        List<OwnedLand> changedLand = new ArrayList<>();
        for (OwnedLand land : LandManager.getInstance().getClaimedLand(village)) {
            boolean changed = false;
            land.setForSale(false);
            if (land.isPlot()) {
                changed = true;
                LandManager.getInstance().setPlotOwner(land, null);
            } else {
                if (land.getBuildState() != BuildState.PUBLIC) {
                    changed = true;
                    land.setBuildState(BuildState.PUBLIC);
                }
                for (Use u : Use.values()) {
                    changed |= land.unsetUseState(u);
                }
            }
            changed |= land.setMode(PlotMode.pvp, true);
            changed |= land.setMode(PlotMode.fireSpread, true);
            changed |= land.setMode(PlotMode.explosions, true);
            changed |= land.setMode(PlotMode.buff, false);
            changed |= land.setMode(PlotMode.mobSpawning, true);
            changed |= land.setMode(PlotMode.embassy, false);
            if (changed) {
                changedLand.add(land);
            }
        }

        for (LockProvider lockProvider : KingdomsPlugin.getInstance().getLockProviders()) {
            lockProvider.removeInChunks(LandManager.getInstance().getClaimedLand(village));
        }
        for (Perk perk : village.getPerks()) {
            if (perk.hasEffects()) {
                PerkData perkData = village.getPerkData(perk);
                if (perkData != null && perkData.isActive()) {
                    perkData.toggle();
                    village.removeEffects(perk);
                }
            }
        }
        village.getPerksMap().clear();

        village.setCreated(System.currentTimeMillis());
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveVillage(village);
            DatabaseManager.getInstance().save(allies);
            DatabaseManager.getInstance().saveResident(residents);
            DatabaseManager.getInstance().saveLand(changedLand);
            Utils.runLater(() -> {
                KingdomsPlugin.getInstance().getLogger().log(Level.INFO, village.getStateTag() + " " + village.getName() + " was removed because it has been abandoned for " + Utils.getAbandonTime(village.getStateKey()) + " minutes!");
                deleteVillage(village);
            }, Utils.getAbandonTime(village.getStateKey()), TimeUnit.MINUTES);
        });
    }

    /**
     * Use {@link Village#removeMember(Targetable)} instead!
     */
    void leaveVillage(Resident resident) {
        Collection<OwnedLand> lands = LandManager.getInstance().getOwnedPlot(resident.getUUID());
        Village village = resident.getVillage();
        for (OwnedLand land : lands) {
            if (land.isOwnedBy(village)) {
                LandManager.getInstance().setPlotOwner(land, null);
            }
        }
        removeResidentFromVillage(resident);
        resident.removeChannel(ChatChannel.SETTLEMENT);
        resident.removeChannel(ChatChannel.EMPIRE);
        Player player = resident.getPlayer();
        if (player != null) {
            Empire empire = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
            if (empire != null && !empire.canAccess(player)) {
                Utils.teleportToSpawn(player);
            }
        }
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().saveResident(resident);
        });
    }

    private void removeResidentFromVillage(Resident resident) {
        if (resident.getVillage().hasEmpire()) {
            resident.getVillage().getEmpire().removeSenator(resident);
        }
        resident.setVillage(null);
        resident.setVillageRank(VillageRank.getDefault());
        resident.setEmpireRank(EmpireRank.getDefault());
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

        for (LevelPerk perk : getPerks()) {
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

    public LevelPerk getPerk(String name) {
        LevelPerk perk = perks.get(name.toLowerCase());
        if (perk == null) {
            perk = perkTags.get(name.toLowerCase());
        }
        return perk;
    }

    public Collection<LevelPerk> getPerks() {
        return perks.values();
    }

    public void changeName(Village village, String name) {
        villages.remove(village.getName().replace(' ', '_').toLowerCase());
        village.setName(name);
        villages.put(village.getName().replace(' ', '_').toLowerCase(), village);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
    }
}
