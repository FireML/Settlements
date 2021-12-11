package de.themoep.settlements.settlement;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandManager;
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
import de.themoep.settlements.rank.SettlementRank;
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

public class SettlementManager {
    private static SettlementManager instance;
    private Map<String, Settlement> settlements = new ConcurrentHashMap<>();
    private Map<IdObject, Settlement> settlementIDs = new ConcurrentHashMap<>();
    private Map<UUID, Settlement> settlementUuids = new ConcurrentHashMap<>();

    private Map<SettlementState, List<SettlementLevel>> levels = new HashMap<>();

    private Map<String, LevelPerk> perks;
    private Map<String, LevelPerk> perkTags;

    private SettlementManager() {
        loadConfig();
        for (Settlement settlement : DatabaseManager.getInstance().getAllSettlements()) {
            settlements.put(settlement.getName().replace(' ', '_').toLowerCase(), settlement);
            settlementIDs.put(settlement.getId(), settlement);
            settlementUuids.put(settlement.getUUID(), settlement);
            if (settlement.isAbandoned()) {
                long timeAbandoned = System.currentTimeMillis() - settlement.getCreated();
                long abandonTime = TimeUnit.MINUTES.toMillis(Config.Settlement.Upkeep.ABANDON_TIME);
                long delay = timeAbandoned > abandonTime ? 0 : abandonTime - timeAbandoned;
                SettlementsPlugin.logDebug(settlement.getStateTag() + " " + settlement.getName() + " will be removed after " + Config.Settlement.Upkeep.ABANDON_TIME + " minutes in " + delay + "!");
                Utils.runLater(() -> {
                    SettlementsPlugin.getInstance().getLogger().log(Level.INFO, settlement.getStateTag() + " " + settlement.getName() + " was removed because it has been abandoned for " + Config.Settlement.Upkeep.ABANDON_TIME + " minutes!");
                    delete(settlement);
                }, delay, TimeUnit.MILLISECONDS);
            }
        }
        SettlementsPlugin.logDebug("Loaded SettlementManager with " + settlementIDs.size() + " settlements and " + perks.size() + " perks");
    }

    private void checkForMissionDistribution() {
        for (Settlement settlement : getSettlements()) {
            if (settlement.hasEmpire() && settlement.getMissionData().isEmpty()
                    && settlement.getEmpire().distributeNewMissions(settlement)) {
                DatabaseManager.getInstance().saveMissionData(settlement);
            }
        }
    }

    public void loadConfig() {
        loadLevels();
        loadPerks();
    }

    private void loadLevels() {
        levels.clear();
        List<SettlementLevel> settlementLevels = new ArrayList<>();
        for (int i = 1; i <= Config.Settlement.Leveling.VILLAGE_MAX_LEVEL; i++) {
            SettlementLevel level = new SettlementLevel(i, ConfigManager.getInstance().getLevelConfig().getConfigurationSection("village.level-" + i));
            settlementLevels.add(level);
            SettlementsPlugin.logDebug("Loaded " + level);
        }
        levels.put(SettlementState.VILLAGE, settlementLevels);

        List<SettlementLevel> kingdomLevels = new ArrayList<>();
        for (int i = 1; i <= Config.Settlement.Leveling.KINGDOM_MAX_LEVEL; i++) {
            SettlementLevel level = new SettlementLevel(i, ConfigManager.getInstance().getLevelConfig().getConfigurationSection("kingdom.level-" + i));
            kingdomLevels.add(level);
            SettlementsPlugin.logDebug("Loaded " + level);
        }
        levels.put(SettlementState.KINGDOM, kingdomLevels);
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

    synchronized public static SettlementManager getInstance() {
        if (instance == null) {
            instance = new SettlementManager();
            instance.checkForMissionDistribution();
        }
        return instance;
    }

    public Collection<Settlement> getSettlements() {
        return settlementIDs.values();
    }

    public void registerNew(Resident chief, Settlement settlement) {
        if (!settlement.getId().isNull()) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving " + settlement.getName() + " to the database! It already had an ID defined?");
            chief.tell(ChatColor.RED + "Error while saving " + settlement.getName() + " to the database! It already had an ID defined?");
            return;
        }
        settlement.setBank(Config.Settlement.START_BALANCE);
        settlements.put(settlement.getName().replace(' ', '_').toLowerCase(), settlement);
        chief.setSettlement(settlement);
        chief.setSettlementRank(SettlementRank.getChief());
        chief.addChannel(ChatChannel.SETTLEMENT);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().addNewSettlement(settlement);
            if (!settlement.getId().isNull()) {
                settlementIDs.put(settlement.getId(), settlement);
                settlementUuids.put(settlement.getUUID(), settlement);
                DatabaseManager.getInstance().saveResident(chief);
            } else {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Error while saving " + settlement.getName() + " to the database! No ID was returned.");
                chief.tell(ChatColor.RED + "Error while saving " + settlement.getName() + " to the database! No ID was returned.");
            }
        });
    }

    public Settlement getAt(Location location) {
        OwnedLand ownedLand = LandManager.getInstance().getLand(location);
        return ownedLand != null && ownedLand.getLandOwner() instanceof Settlement ? (Settlement) ownedLand.getLandOwner() : null;
    }

    public Settlement getFromId(IdObject id) {
        if (id == null || id.isNull()) {
            return null;
        }
        return settlementIDs.get(id);
    }

    public Settlement getFromUuid(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return settlementUuids.get(uuid);
    }

    public Settlement getFromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return settlements.get(name.replace(' ', '_').toLowerCase());
    }

    public Settlement getFromResident(Resident res) {
        return res != null ? res.getSettlement() : null;
    }

    public Settlement getFromPlayer(Player player) {
        return this.getFromResident(ResidentManager.getInstance().getResident(player));
    }

    public void delete(Settlement settlement) {
        Utils.callEvent(new LandownerDisbandEvent(settlement));
        settlements.remove(settlement.getName().replace(' ', '_').toLowerCase());
        settlementIDs.remove(settlement.getId());
        settlementUuids.remove(settlement.getUUID());
        if (settlement.hasEmpire()) {
            settlement.getEmpire().getMemberIds().remove(settlement.getId());
        }
        SettlementsPlugin.getInstance().addNewlyDeleted(settlement);

        List<Resident> residents = settlement.getResidents();
        for (Resident resident : residents) {
            removeResidentFromSettlement(resident);
        }
        LandManager.getInstance().unclaimLand(settlement);
        DatabaseManager.getInstance().saveResident(residents);
        Utils.runAsync(() -> {
            DatabaseManager.getInstance().deleteSettlement(settlement);
            for (Ally ally : settlement.getAllies()) {
                ally.removeAlly(settlement);
            }
        });
    }

    void abandon(Settlement settlement) {
        if (settlement.hasEmpire()) {
            settlement.getEmpire().getMemberIds().remove(settlement.getId());
        }
        SettlementsPlugin.getInstance().addNewlyAbandoned(settlement);

        List<Resident> residents = settlement.getResidents();
        for (Resident resident : residents) {
            removeResidentFromSettlement(resident);
        }
        settlement.getResidentIds().clear();
        Collection<Ally> allies = settlement.getAllies();
        settlement.getAllies().clear();
        for (Ally ally : allies) {
            ally.removeAlly(settlement);
        }
        settlement.setAccess(LandAccess.PRIVATE);
        settlement.setBuildState(BuildState.PUBLIC);
        for (Use u : Use.values()) {
            settlement.setUseState(u, BuildState.PUBLIC);
        }
        for (PlotMode mode : PlotMode.values()) {
            if (mode.appliesTo(PlotMode.AppliesTo.LAND_OWNER)) {
                settlement.setMode(mode, Utils.getPlotMode(mode, "abandoned." + settlement.getStateKey()));
            }
        }

        List<OwnedLand> changedLand = new ArrayList<>();
        for (OwnedLand land : LandManager.getInstance().getClaimedLand(settlement)) {
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
            lockProvider.removeInChunks(LandManager.getInstance().getClaimedLand(settlement));
        }
        for (Perk perk : settlement.getPerks()) {
            if (perk.hasEffects()) {
                PerkData perkData = settlement.getPerkData(perk);
                if (perkData != null && perkData.isActive()) {
                    perkData.toggle();
                    settlement.removeEffects(null, perk);
                }
            }
        }
        settlement.getPerksMap().clear();

        settlement.setCreated(System.currentTimeMillis());
        DatabaseManager.getInstance().saveSettlement(settlement);
        DatabaseManager.getInstance().saveAllies(allies);
        DatabaseManager.getInstance().saveResident(residents);
        DatabaseManager.getInstance().saveLand(changedLand);
        Utils.runLater(() -> {
            SettlementsPlugin.getInstance().getLogger().log(Level.INFO, settlement.getStateTag() + " " + settlement.getName() + " was removed because it has been abandoned for " + Config.Settlement.Upkeep.ABANDON_TIME + " minutes!");
            delete(settlement);
            MessageUtils.broadcast(Message.get("broadcast.settlement-deletion"),
                    "name", settlement.getName(),
                    "state", settlement.getState().getTag());
        }, Config.Settlement.Upkeep.ABANDON_TIME, TimeUnit.MINUTES);
    }

    /**
     * Use {@link Settlement#removeMember(Targetable)} instead!
     */
    void leaveSettlement(Resident resident) {
        Collection<OwnedLand> lands = LandManager.getInstance().getOwnedPlot(resident.getUUID());
        Settlement settlement = resident.getSettlement();
        for (OwnedLand land : lands) {
            if (land.isOwnedBy(settlement) && !land.isEmbassy()) {
                LandManager.getInstance().setPlotOwner(land, null);
            }
        }
        removeResidentFromSettlement(resident);
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

    private void removeResidentFromSettlement(Resident resident) {
        resident.setSettlement(null);
        resident.setSettlementRank(SettlementRank.getDefault());
        resident.setEmpireRank(EmpireRank.getDefault());
    }

    /**
     * Check whether a settlement can level up
     * @param settlement The settlement to check
     * @param res
     * @return Whether the settlement was leveled up
     */
    public boolean checkLevelUp(Settlement settlement, Resident res) {
        if (!settlement.canLevelUp()) {
            return false;
        }

        if (settlement.getLevel() == Config.Settlement.Leveling.VILLAGE_MAX_LEVEL && settlement.getState() == SettlementState.VILLAGE) {
            settlement.setState(SettlementState.KINGDOM);
            settlement.setLevel(1);
            MessageUtils.tellResidents(settlement.getOnlineResidents(), Message.get("levels.upgrade-settlement"), "state", settlement.getState().getTag());
        } else {
            settlement.setLevel(settlement.getLevel() + 1);
            MessageUtils.tellResidents(settlement.getOnlineResidents(), Message.get("levels.upgrade"), "level", Integer.toString(settlement.getLevel()), "state", settlement.getState().getTag());
        }
        removeLevelRequirements(settlement, res);

        for (LevelPerk perk : getPerks()) {
            if (settlement.getState() == perk.getState() && settlement.getLevel() == perk.getLevel()) {
                if (perk.getUnlockCost().isEmpty()) {
                    MessageUtils.tellResidents(settlement.getOnlineResidents(), Message.get("levels.perk-unlocked"), "perk", perk.getTag());
                    settlement.unlockPerk(res.getPlayer(), perk);
                } else {
                    MessageUtils.tellResidents(settlement.getOnlineResidents(), Message.get("levels.perk-available"), "perk", perk.getTag());
                }
            }
        }
        return true;
    }

    private void removeLevelRequirements(Settlement settlement, Resident res) {
        Map<String, Long> levelReqs = getLevel(settlement.getState(), settlement.getLevel())
                .map(SettlementLevel::getRequirements)
                .orElse(Collections.emptyMap());
        if (settlement.remove(levelReqs)) {
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveSettlement(settlement);
                DatabaseManager.getInstance().logLevelup(settlement, res, levelReqs);
            });
        }
    }

    public Optional<SettlementLevel> getLevel(SettlementState state, int level) {
        List<SettlementLevel> levels = this.levels.get(state);
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

    public void changeName(Settlement settlement, String name) {
        settlements.remove(settlement.getName().replace(' ', '_').toLowerCase());
        settlement.setName(name);
        settlements.put(settlement.getName().replace(' ', '_').toLowerCase(), settlement);
        DatabaseManager.getInstance().saveSettlement(settlement);
    }
}
