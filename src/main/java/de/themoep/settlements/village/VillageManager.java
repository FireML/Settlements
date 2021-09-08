package de.themoep.settlements.village;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.Use;
import de.themoep.settlements.land.events.LandownerDisbandEvent;
import de.themoep.settlements.lock.LockProvider;
import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.rank.VillageRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class VillageManager {
    private static VillageManager instance;
    private Map<String, Village> settlements = new ConcurrentHashMap<>();
    private Map<IdObject, Village> settlementIDs = new ConcurrentHashMap<>();
    private Map<UUID, Village> settlementUuids = new ConcurrentHashMap<>();

    private Map<VillageState, List<VillageLevel>> levels = new HashMap<>();

    private Map<String, LevelPerk> perks;
    private Map<String, LevelPerk> perkTags;

    private VillageManager() {
        loadConfig();
        for (Village village : DatabaseManager.getInstance().getAllSettlements()) {
            settlements.put(village.getName().replace(' ', '_').toLowerCase(), village);
            settlementIDs.put(village.getId(), village);
            settlementUuids.put(village.getUUID(), village);
            if (village.isAbandoned()) {
                long timeAbandoned = System.currentTimeMillis() - village.getCreated();
                long abandonTime = TimeUnit.MINUTES.toMillis(Config.Settlement.Upkeep.ABANDON_TIME);
                long delay = timeAbandoned > abandonTime ? 0 : abandonTime - timeAbandoned;
                SettlementsPlugin.logDebug(village.getStateTag() + " " + village.getName() + " will be removed after " + Config.Settlement.Upkeep.ABANDON_TIME + " minutes in " + delay + "!");
                Utils.runLater(() -> {
                    SettlementsPlugin.getInstance().getLogger().log(Level.INFO, village.getStateTag() + " " + village.getName() + " was removed because it has been abandoned for " + Config.Settlement.Upkeep.ABANDON_TIME + " minutes!");
                    delete(village);
                }, delay, TimeUnit.MILLISECONDS);
            }
        }
        SettlementsPlugin.logDebug("Loaded VillageManager with " + settlementIDs.size() + " empires and " + perks.size() + " perks");
    }

    private void checkForMissionDistribution() {
        for (Village village : getSettlements()) {
            if (village.hasEmpire() && village.getMissionData().isEmpty()
                    && village.getEmpire().distributeNewMissions(village)) {
                DatabaseManager.getInstance().saveMissionData(village);
            }
        }
    }

    public void loadConfig() {
        loadLevels();
        loadPerks();
    }

    private void loadLevels() {
        levels.clear();
        List<VillageLevel> villageLevels = new ArrayList<>();
        for (int i = 1; i <= Config.Settlement.Leveling.VILLAGE_MAX_LEVEL; i++) {
            VillageLevel level = new VillageLevel(i, ConfigManager.getInstance().getLevelConfig().getConfigurationSection("village.level-" + i));
            villageLevels.add(level);
            SettlementsPlugin.logDebug("Loaded " + level);
        }
        levels.put(VillageState.VILLAGE, villageLevels);

        List<VillageLevel> kingdomLevels = new ArrayList<>();
        for (int i = 1; i <= Config.Settlement.Leveling.KINGDOM_MAX_LEVEL; i++) {
            VillageLevel level = new VillageLevel(i, ConfigManager.getInstance().getLevelConfig().getConfigurationSection("kingdom.level-" + i));
            kingdomLevels.add(level);
            SettlementsPlugin.logDebug("Loaded " + level);
        }
        levels.put(VillageState.KINGDOM, kingdomLevels);
    }

    private void loadPerks() {
        perks = new LinkedHashMap<>();
        perkTags = new LinkedHashMap<>();
        for (String perkName : ConfigManager.getInstance().getPerksConfig().getKeys(false)) {
            try {
                LevelPerk perk = new LevelPerk(ConfigManager.getInstance().getPerksConfig().getConfigurationSection(perkName));
                perks.put(perk.name().toLowerCase(), perk);
                perkTags.put(perk.getTag().toLowerCase(), perk);
            } catch (IllegalArgumentException e) {
                SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Config for Perk " + perkName + " was invalid! " + e.getMessage());
            }
        }
    }

    synchronized public static VillageManager getInstance() {
        if (instance == null) {
            instance = new VillageManager();
            instance.checkForMissionDistribution();
        }
        return instance;
    }

    public Collection<Village> getSettlements() {
        return settlementIDs.values();
    }

    public void registerNew(Resident chief, Village village) {
        if (!village.getId().isNull()) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving " + village.getName() + " to the database! It already had an ID defined?");
            chief.tell(ChatColor.RED + "Error while saving " + village.getName() + " to the database! It already had an ID defined?");
            return;
        }
        village.setBank(Config.Settlement.START_BALANCE);
        settlements.put(village.getName().replace(' ', '_').toLowerCase(), village);
        chief.setVillage(village);
        chief.setVillageRank(VillageRank.getChief());
        chief.addChannel(ChatChannel.SETTLEMENT);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().addNewVillage(village);
            if (!village.getId().isNull()) {
                settlementIDs.put(village.getId(), village);
                settlementUuids.put(village.getUUID(), village);
                DatabaseManager.getInstance().saveResident(chief);
            } else {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving " + village.getName() + " to the database! No ID was returned.");
                chief.tell(ChatColor.RED + "Error while saving " + village.getName() + " to the database! No ID was returned.");
            }
        });
    }

    public Village getAt(Location location) {
        OwnedLand ownedLand = LandManager.getInstance().getLand(location);
        return ownedLand != null && ownedLand.getLandOwner() instanceof Village ? (Village) ownedLand.getLandOwner() : null;
    }

    public Village getFromId(IdObject id) {
        if (id == null || id.isNull()) {
            return null;
        }
        return settlementIDs.get(id);
    }

    public Village getFromUuid(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return settlementUuids.get(uuid);
    }

    public Village getFromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return settlements.get(name.replace(' ', '_').toLowerCase());
    }

    public Village getFromResident(Resident res) {
        return res != null ? res.getVillage() : null;
    }

    public Village getFromPlayer(Player player) {
        return this.getFromResident(ResidentManager.getInstance().getResident(player));
    }

    public void delete(Village village) {
        Utils.callEvent(new LandownerDisbandEvent(village));
        settlements.remove(village.getName().replace(' ', '_').toLowerCase());
        settlementIDs.remove(village.getId());
        settlementUuids.remove(village.getUUID());
        if (village.hasEmpire()) {
            village.getEmpire().getMemberIds().remove(village.getId());
        }
        SettlementsPlugin.getInstance().addNewlyDeleted(village);

        List<Resident> residents = village.getResidents();
        for (Resident resident : residents) {
            removeResidentFromVillage(resident);
        }
        LandManager.getInstance().unclaimLand(village);
        DatabaseManager.getInstance().saveResident(residents);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().deleteVillage(village);
            for (LandOwner ally : village.getAllies()) {
                ally.removeAlly(village);
            }
        });
    }

    void abandon(Village village) {
        if (village.hasEmpire()) {
            village.getEmpire().getMemberIds().remove(village.getId());
        }
        SettlementsPlugin.getInstance().addNewlyAbandoned(village);

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
        for (PlotMode mode : PlotMode.values()) {
            if (mode.appliesTo(PlotMode.AppliesTo.LAND_OWNER)) {
                village.setMode(mode, Utils.getPlotMode(mode, "abandoned." + village.getStateKey()));
            }
        }

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
            for (PlotMode mode : PlotMode.values()) {
                if (mode.appliesTo(PlotMode.AppliesTo.LAND)) {
                    changed |= land.unsetMode(mode);
                }
            }
            if (changed) {
                changedLand.add(land);
            }
        }

        for (LockProvider lockProvider : SettlementsPlugin.getInstance().getLockProviders()) {
            lockProvider.removeInChunks(LandManager.getInstance().getClaimedLand(village));
        }
        for (Perk perk : village.getPerks()) {
            if (perk.hasEffects()) {
                PerkData perkData = village.getPerkData(perk);
                if (perkData != null && perkData.isActive()) {
                    perkData.toggle();
                    village.removeEffects(null, perk);
                }
            }
        }
        village.getPerksMap().clear();

        village.setCreated(System.currentTimeMillis());
        DatabaseManager.getInstance().saveSettlement(village);
        DatabaseManager.getInstance().save(allies);
        DatabaseManager.getInstance().saveResident(residents);
        DatabaseManager.getInstance().saveLand(changedLand);
        Utils.runLater(() -> {
            SettlementsPlugin.getInstance().getLogger().log(Level.INFO, village.getStateTag() + " " + village.getName() + " was removed because it has been abandoned for " + Config.Settlement.Upkeep.ABANDON_TIME + " minutes!");
            delete(village);
            MessageUtils.broadcast(Message.get("broadcast.village-deletion"),
                    "name", village.getName(),
                    "state", village.getState().getTag());
        }, Config.Settlement.Upkeep.ABANDON_TIME, TimeUnit.MINUTES);
    }

    /**
     * Use {@link Village#removeMember(Targetable)} instead!
     */
    void leaveVillage(Resident resident) {
        Collection<OwnedLand> lands = LandManager.getInstance().getOwnedPlot(resident.getUUID());
        Village village = resident.getVillage();
        for (OwnedLand land : lands) {
            if (land.isOwnedBy(village) && !land.isEmbassy()) {
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
        DatabaseManager.getInstance().saveResident(resident);
    }

    private void removeResidentFromVillage(Resident resident) {
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

        if (village.getLevel() == Config.Settlement.Leveling.VILLAGE_MAX_LEVEL && village.getState() == VillageState.VILLAGE) {
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
                    village.unlockPerk(res.getPlayer(), perk);
                } else {
                    MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("levels.perk-available"), "perk", perk.getTag());
                }
            }
        }
        return true;
    }

    private void removeLevelRequirements(Village village, Resident res) {
        Map<String, Long> levelReqs = getLevel(village.getState(), village.getLevel())
                .map(VillageLevel::getRequirements)
                .orElse(Collections.emptyMap());
        if (village.remove(levelReqs)) {
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveSettlement(village);
                DatabaseManager.getInstance().logLevelup(village, res, levelReqs);
            });
        }
    }

    public Optional<VillageLevel> getLevel(VillageState state, int level) {
        List<VillageLevel> levels = this.levels.get(state);
        if (levels != null && level <= levels.size()) {
            return Optional.of(levels.get(level - 1));
        }
        return Optional.empty();
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
        settlements.remove(village.getName().replace(' ', '_').toLowerCase());
        village.setName(name);
        settlements.put(village.getName().replace(' ', '_').toLowerCase(), village);
        DatabaseManager.getInstance().saveSettlement(village);
    }
}
