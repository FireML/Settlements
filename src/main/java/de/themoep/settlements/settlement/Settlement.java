package de.themoep.settlements.settlement;

import co.aikar.timings.lib.MCTiming;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.empire.EmpireMission;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.events.LandownerChiefChangeEvent;
import de.themoep.settlements.missions.Mission;
import de.themoep.settlements.missions.MissionData;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.missions.MissionTaker;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcLevel;
import de.themoep.settlements.npc.NpcManager;
import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.settlement.events.SettlementJoinEvent;
import de.themoep.settlements.settlement.events.SettlementLeaveEvent;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.TaxDebtAction;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.perk.SettlementPerk;
import de.themoep.settlements.rank.RankManager;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Sortable;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import de.themoep.settlements.world.NamedLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static de.themoep.settlements.utils.MessageUtils.yesOrNo;

public class Settlement extends LandOwner<SettlementRank, SettlementLevel>
        implements Comparable<Settlement>, Targetable, MissionProvider<SettlementMission>, MissionTaker<EmpireMission> {
    private int level;
    private UUID chief;
    private Empire empire;
    private SettlementState settlementState;
    private Map<String, NamedLocation> outpostHomes = new LinkedHashMap<>();
    private Set<UUID> residents = new LinkedHashSet<>();
    private long missionPoints = 0;
    private long finishedMissions = 0;
    private Map<String, MissionData> missionData = new LinkedHashMap<>();

    protected Settlement() {
        super();
    }

    public Settlement(String name, UUID chief, int level, SettlementState state) {
        super(name);
        residents.add(chief);
        this.chief = chief;
        this.level = level;
        this.settlementState = state;
    }

    public void save() {
        DatabaseManager.getInstance().saveSettlement(this);
    }

    public void doUpkeep() {
        if (isAbandoned()) {
            SettlementsPlugin.getInstance().getLogger().log(Level.INFO, getStateTag() + " " + getName() + " is abandoned. No upkeep.");
            return;
        }
        int inactiveDays = Config.Settlement.Upkeep.INACTIVE_TIME;
        if (inactiveDays > 0 && getLevel() == 1 && getState() == SettlementState.VILLAGE && !hasEmpire() && getPerksMap().isEmpty()
                && LandManager.getInstance().getClaimedLandSize(this) == 0
                && getChief().getLastLogin() + TimeUnit.DAYS.toMillis(inactiveDays) < System.currentTimeMillis()) {
            OfflinePlayer chiefPlayer = SettlementsPlugin.getInstance().getServer().getOfflinePlayer(getChiefId());
            if (!chiefPlayer.hasPlayedBefore() || chiefPlayer.getLastPlayed() + TimeUnit.DAYS.toMillis(inactiveDays) < System.currentTimeMillis()) {
                SettlementsPlugin.getInstance().getLogger().log(Level.INFO, getStateTag() + " " + getName() + " is inactive " +
                        "(chief offline for more than " + inactiveDays + ", Village level 1, no claims, no perks unlocked), removing it!");
                SettlementManager.getInstance().delete(this);
                return;
            }
        }
        Map<Resident, Long> taxes = new LinkedHashMap<>();
        if (getTax() > 0) {
            long tax = getTax();
            for (Resident resident : getResidents()) { // residents should already be cached!
                if (isChief(resident)) {
                    continue;
                }
                if (resident.hasRankPermission("settlement.tax.exempt") || resident.isTaxExempted()) {
                    continue;
                }
                taxes.put(resident, tax);
            }
        }

        for (Map.Entry<Resident, Long> entry : getLandTaxes().entrySet()) {
            taxes.merge(entry.getKey(), entry.getValue(), (l1, l2) -> l1 + l2);
        }

        long totalTax = 0;
        for (Map.Entry<Resident, Long> entry : taxes.entrySet()) {
            Resident resident = entry.getKey();
            long tax = entry.getValue();
            OfflinePlayer player = resident.getPlayer();
            if (player == null) {
                player = Bukkit.getOfflinePlayer(resident.getUUID());
            }
            if (SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(player, tax).transactionSuccess()) {
                resident.addTaxPaid(tax);
                resident.save();
                addToBank(tax);
                totalTax += tax;
                if (player instanceof Player) {
                    Message.get("tax.removed").tell((Player) player,
                            "amount", String.valueOf(tax),
                            "state", getState().getTag(),
                            "name", getName());
                }
            } else {
                Message.get("tax.debt-action." + getTaxDebtAction().getKey() + ".couldnt-pay").tell(getOnlineResidents(),
                        "player", resident.getName(),
                        "amount", String.valueOf(tax),
                        "state", getState().getTag(),
                        "name", getName()
                );
                if (getTaxDebtAction() == TaxDebtAction.KICK && resident.getTaxDebtAction() == TaxDebtAction.KICK) {
                    if (isMember(resident)) {
                        addBanished(null, resident, "Tax");
                        removeMember(resident);
                    }
                    for (OwnedLand land : getLand()) {
                        if (land.isOwnedBy(resident)) {
                            LandManager.getInstance().setPlotOwner(land, null);
                        }
                    }
                }
            }
        }

        if (totalTax > 0) {
            long finalTotalTax = totalTax;
            Utils.runAsync(() -> DatabaseManager.getInstance().log(this, null, "tax", "cash", finalTotalTax));
        }
        payUpkeep(totalTax);

        if (distributeNewMissions()) {
            DatabaseManager.getInstance().saveResidentMissionData(getResidents());
        }
    }

    private boolean distributeNewMissions() {
        boolean modified = false;
        for (Resident resident : getResidents()) {
            modified |= distributeNewMissions(resident);
        }
        return modified;
    }

    @Override
    public <T extends MissionTaker<SettlementMission>> boolean distributeNewMissions(T taker) {
        SettlementLevel levelInfo = getLevelInfo();
        if (levelInfo != null && levelInfo.getMissions() > 0) {
            List<EmpireMission> missions = new ArrayList<>(EmpireManager.getInstance().getMissions());
            taker.getMissionData().removeIf(md -> {
                if (md.isFinished()) {
                    return Config.Settlement.Missions.REMOVE_FINISHED_MISSIONS;
                }
                if (md.hasProgress()) {
                    return Config.Settlement.Missions.REMOVE_STARTED_MISSIONS;
                }
                return Config.Settlement.Missions.REMOVE_NOT_STARTED_MISSIONS;
            });
            Collections.shuffle(missions);
            int toDistribute = levelInfo.getMissions() + taker.getExtraMissions();
            for (int i = 0; taker.getMissionData().size() < toDistribute && i < missions.size(); i++) {
                taker.addMissionData(new MissionData(missions.get(i)));
            }
            return !taker.getMissionData().isEmpty();
        }
        return false;
    }

    private Map<Resident, Long> getLandTaxes() {
        Map<Resident, Long> taxes = new LinkedHashMap<>();
        for (OwnedLand land : getLand()) {
            if (land.isPlot() && land.getTax() > 0) {
                Resident resident = ResidentManager.getInstance().getResident(land.getPlotOwner());
                if (isChief(resident)) {
                    continue;
                }
                if (isMember(resident) && resident.hasRankPermission(getKey() + ".tax.exempt")) {
                    continue;
                }
                taxes.put(resident, taxes.computeIfAbsent(resident, r -> 0L) + land.getTax());
            }
        }
        return taxes;
    }

    private void payUpkeep(long lastTax) {
        if (Config.Empire.Upkeep.EXEMPT_CAPITAL && hasEmpire() && getEmpire().isCapital(this)) {
            // no upkeep for empire capitals
            if (lastTax > 0) {
                DatabaseManager.getInstance().saveSettlement(this);
            }
            return;
        }
        long total = getUpkeep();
        if (removeFromBank(total)) {
            SettlementsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") was charged an upkeep of " + total + "!");
            MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.charged"), "charged", String.valueOf(total));
            if (!has(total - lastTax)) {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.cant-pay"), "amount", String.valueOf(total));
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveSettlement(this);
                DatabaseManager.getInstance().log(this, null, "upkeep", "cash", total);
            });
        } else {
            if (getLevel() > 1 || getState() == SettlementState.KINGDOM) {
                if (getLevel() == 1 && getState() == SettlementState.KINGDOM) {
                    SettlementsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and has been downgraded!");
                    setState(SettlementState.VILLAGE);
                    setLevel(Config.Settlement.Leveling.VILLAGE_MAX_LEVEL);
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.downgraded"), "state", getState().getTag(), "level", String.valueOf(getLevel()));
                } else {
                    SettlementsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and has had a level removed!");
                    setLevel(getLevel() - 1);
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.level-removed"), "level", String.valueOf(getLevel()));
                }
                DatabaseManager.getInstance().saveSettlement(this);
            } else {
                setBank(0);
                if (Config.Settlement.Upkeep.ABANDON_TIME > 0) {
                    SettlementsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and was abandoned!");
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.abandoned"));
                    SettlementManager.getInstance().abandon(this);
                } else {
                    SettlementsPlugin.getInstance().getLogger().log(Level.INFO, getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and was deleted!");
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.deleted"));
                    SettlementManager.getInstance().delete(this);
                }
            }
        }
    }

    @Override
    public long getUpkeep() {
        if (Config.Empire.Upkeep.EXEMPT_CAPITAL && hasEmpire() && getEmpire().isCapital(this)) {
            return 0;
        }
        long amount = 0;
        for (OwnedLand land : LandManager.getInstance().getClaimedLand(this)) {
            amount += getClaimUpkeep(land.getWorld());
        }
        for (NpcData data : getNpcData()) {
            NpcLevel npcLevel = data.getLevelInfo();
            if (npcLevel != null) {
                amount += data.getLevelInfo().getUpkeep();
            }
        }
        return amount;
    }

    @Override
    public long getClaimUpkeep(String world) {
        return Math.max(0, SettlementManager.getInstance().getLevel(getState(), getLevel()).map(l -> l.getClaimUpkeep(world)).orElse(0L)
                - (hasEmpire() ? getEmpire().getClaimUpkeepReduction() : 0));
    }

    @Override
    public boolean isAbandoned() {
        return residents.isEmpty();
    }

    @Override
    public long getAbandonTimeConfig() {
        return Config.Settlement.Upkeep.ABANDON_TIME;
    }

    @Override
    public boolean canClaim(World world) {
        return !Config.Settlement.Claiming.DISABLED_WORLDS.contains(world.getName())
                && !Config.Claiming.DISABLED_WORLDS.contains(world.getName())
                && EmpireManager.getInstance().getEmpireFromWorld(world) == null;
    }

    public void setOutpostHome(String name, Location loc) {
        setOutpostHome(new NamedLocation(name, loc));
    }

    public void setOutpostHome(NamedLocation location) {
        outpostHomes.put(location.getName().toLowerCase(Locale.ROOT), location);
    }

    public NamedLocation removeOutpostHome(NamedLocation location) {
        return outpostHomes.remove(location.getName().toLowerCase(Locale.ROOT));
    }

    public Map<String, NamedLocation> getOutpostHomes() {
        return outpostHomes;
    }

    @Override
    public boolean isTrusted(LandOwner landOwner) {
        return (landOwner instanceof Empire && landOwner.equals(getEmpire()))
                || (landOwner instanceof Settlement && hasEmpire() && getEmpire().isMember((Settlement) landOwner));
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
        if (!ConfigManager.getInstance().getDataConfig().getConfig().contains(settlementState.getKey() + ".level" + level)) {
            ConfigManager.getInstance().getDataConfig().getConfig().set(settlementState.getKey() + ".level" + level, getName());
            ConfigManager.getInstance().getDataConfig().saveConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                MessageUtils.tell(player, Message.get("first-to-reach-level"),
                        "name", this.getName(),
                        "state", settlementState.name().toLowerCase(),
                        "level", String.valueOf(level));
            }
        }
    }

    @Override
    @Nullable
    public SettlementLevel getLevelInfo() {
        return SettlementManager.getInstance().getLevel(getState(), getLevel()).orElse(null);
    }

    @Override
    public Optional<SettlementLevel> getNextLevel() {
        int nextLevel = getLevel() + 1;
        SettlementState nextState = getState();
        if (nextState == SettlementState.VILLAGE && nextLevel > Config.Settlement.Leveling.VILLAGE_MAX_LEVEL) {
            nextState = SettlementState.KINGDOM;
            nextLevel = 1;
        }
        return SettlementManager.getInstance().getLevel(nextState, nextLevel);
    }

    public long getMissionPoints() {
        return missionPoints;
    }

    public void setMissionPoints(long missionPoints) {
        this.missionPoints = missionPoints;
    }

    public int getExtraMissions() {
        SettlementLevel settlementLevel = getLevelInfo();
        return settlementLevel != null ? settlementLevel.getExtraMissions() : 0;
    }

    @Override
    public boolean isChief(UUID playerId) {
        return chief.equals(playerId);
    }

    public UUID getChiefId() {
        return chief;
    }

    public void setChief(UUID chief) {
        this.chief = chief;
    }

    /**
     * Get the chief, might run database operation!
     * @return The chief's Resident object
     */
    public Resident getChief() {
        return ResidentManager.getInstance().getResident(getChiefId());
    }

    /**
     * Might run database operation!
     */
    @Override
    public List<Resident> getResidents() {
        return residents.stream()
                .map(id -> ResidentManager.getInstance().getResident(id)).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resident> getOnlineResidents() {
        return residents.stream()
                .map(Bukkit::getPlayer).filter(Objects::nonNull)
                .map(p -> ResidentManager.getInstance().getResident(p))
                .collect(Collectors.toList());
    }

    @Override
    public long getOnlineCount() {
        return residents.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).count();
    }

    @Override
    public boolean hasPermission(Resident resident, String perm) {
        return isMember(resident) && resident.hasPermission("settlement." + perm);
    }

    @Override
    public boolean isOfficer(Player player) {
        Resident res = ResidentManager.getInstance().getResident(player);
        return hasPermission(res, "officer");
    }

    @Override
    public boolean isMember(Resident resident) {
        return equals(resident.getSettlement());
    }

    @Override
    public String getStateTag() {
        return getState().getTag();
    }

    @Override
    public MissionTaker<SettlementMission> getMissionTaker(Resident resident) {
        return resident;
    }

    @Override
    public String getStateKey() {
        return getState().getKey();
    }

    @Override
    public Resident getResident(String name) {
        for (Resident resident : getResidents()) {
            if (resident.getName().equalsIgnoreCase(name)) {
                return resident;
            }
        }
        return null;
    }

    public Set<UUID> getResidentIds() {
        return residents;
    }

    public boolean isFull() {
        SettlementLevel level = getLevelInfo();
        if (level != null) {
            int levelSize = level.getSize();
            if (levelSize > 0) {
                return getResidentIds().size() >= levelSize;
            }
        }
        return Config.Settlement.MAX_SIZE > 0 && getResidentIds().size() >= Config.Settlement.MAX_SIZE;
    }

    public SettlementState getState() {
        return settlementState;
    }

    public void setState(SettlementState state) {
        this.settlementState = state;
    }

    @Override
    public int getMaxClaimableChunks() {
        return SettlementManager.getInstance().getLevel(getState(), getLevel()).map(SettlementLevel::getChunks).orElse(0)
                + getBonusClaimAmount();
    }

    @Override
    public int getMaxSize() {
        int maxSize = getLevelInfo() != null ? getLevelInfo().getSize() : -1;
        return maxSize > 0 ? maxSize : Config.Settlement.MAX_SIZE;
    }

    public int getBonusClaimAmount() {
        return getBonusClaims() + getPerksMap().entrySet().stream()
                .filter(e -> getPerk(e.getKey()) != null)
                .mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusClaims())
                .sum()
                + (hasEmpire() ? getEmpire().getBonusClaimAmount() : 0);
    }

    public int getMaxOutposts() {
        return SettlementManager.getInstance().getLevel(getState(), getLevel()).map(SettlementLevel::getOutposts).orElse(0)
                + getBonusOutposts() + (hasEmpire() ? getEmpire().getBonusOutposts() : 0);
    }

    public int getBonusOutposts() {
        return getPerksMap().entrySet().stream()
                .filter(e -> getPerk(e.getKey()) != null)
                .mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusOutposts())
                .sum();
    }

    public Empire getEmpire() {
        return empire;
    }

    public void setEmpire(Empire empire) {
        if (getEmpire() != null) {
            getEmpire().getMemberIds().remove(getId());
        }
        this.empire = empire;
        if (empire != null) {
            empire.getMemberIds().add(getId());
        } else {
            getMissionData().clear();
        }
    }

    public boolean hasEmpire() {
        return empire != null;
    }

    public MissionData getMissionData(String missionId) {
        return missionData.get(missionId.toLowerCase(Locale.ROOT));
    }

    public Collection<MissionData> getMissionData() {
        return missionData.values();
    }

    public Map<String, Map<String, Long>> getMissionDataMap() {
        return getMissionData().stream().collect(Collectors.toMap(md -> md.getMission().getDbId(), MissionData::getProgress));
    }

    public void addMissionData(MissionData md) {
        missionData.put(md.getMission().getId().toLowerCase(Locale.ROOT), md);
    }

    @NotNull
    @Override
    public Map<String, Long> getNextLevelRequirements() {
        return getNextLevel().map(SettlementLevel::getRequirements).orElse(Collections.emptyMap());
    }

    @Override
    public boolean isMaxLevel() {
        return (getState().atLeast(SettlementState.KINGDOM) && getLevel() >= Config.Settlement.Leveling.KINGDOM_MAX_LEVEL) || getNextLevelRequirements().isEmpty();
    }

    @Override
    public boolean checkLevelUp(Resident resident) {
        return SettlementManager.getInstance().checkLevelUp(this, resident);
    }

    @Override
    public Collection<? extends LevelPerk> getPerks(int level) {
        return SettlementManager.getInstance().getPerks().stream().filter(p -> p.getLevel() == level && p.getState() == settlementState).collect(Collectors.toList());
    }

    @Override
    public long getAmount(String key) {
        if (key.equalsIgnoreCase("level")) {
            return getState() == SettlementState.VILLAGE ? getLevel() : Config.Settlement.Leveling.VILLAGE_MAX_LEVEL + getLevel();
        }
        if (key.equalsIgnoreCase("missions")) {
            return finishedMissions;
        }
        if (key.equalsIgnoreCase("missionpoints")) {
            return missionPoints;
        }
        return super.getAmount(key);
    }

    @Override
    public void setAmount(String key, long amount) {
        if ("missions".equalsIgnoreCase(key)) {
            setFinishedMissions(amount);
        } else if ("missionpoints".equalsIgnoreCase(key)) {
            setMissionPoints(amount);
        } else {
            super.setAmount(key, amount);
        }
    }

    public long getFinishedMissions() {
        return finishedMissions;
    }

    public void setFinishedMissions(long finishedMissions) {
        this.finishedMissions = finishedMissions;
    }

    @Override
    public boolean setMode(PlotMode mode, boolean value) {
        if (super.setMode(mode, value)) {
            if (SettlementsPlugin.getInstance().isRunning() && mode == PlotMode.mobSpawning && !value) {
                try (MCTiming ignored = SettlementsPlugin.getInstance().getAndStartTiming("Settlement mob spawning mode toggle")) {
                    for (OwnedLand land : LandManager.getInstance().getClaimedLand(this)) {
                        World world = Bukkit.getWorld(land.getWorldName());
                        if (world != null && world.isChunkLoaded(land.getX(), land.getZ())) {
                            for (Entity entity : land.getChunk().getEntities()) {
                                if (entity instanceof LivingEntity && Utils.isHostile((LivingEntity) entity)) {
                                    entity.remove();
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Collection<? extends Perk> getAvailablePerks() {
        List<Perk> perks = new ArrayList<>();
        perks.addAll(SettlementManager.getInstance().getPerks());
        perks.addAll(NpcManager.getInstance().getPerks(this));
        return perks;
    }

    @Override
    public SettlementPerk getPerk(String name) {
        return SettlementManager.getInstance().getPerk(name);
    }

    @EventHandler
    public boolean isUnlockable(String unlockable) {
        return SettlementManager.getInstance().getUnlockables().contains(unlockable);
    }

    @Override
    protected Set<String> getPermissions() {
        return RankManager.getInstance().getSettlementChiefRank().getInheritedPermissions();
    }

    @Override
    public Collection<String> getToggleablePermissions() {
        return RankManager.getInstance().getSettlementToggleablePerms();
    }

    @Override
    public BuildState getDefaultBuildState() {
        return BuildState.RESIDENTS;
    }

    @Override
    public BuildState.AppliesTo getBuildStateType() {
        return BuildState.AppliesTo.SETTLEMENT;
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = super.serialize();
        map.put("chief", getChiefId());
        map.put("level", getLevel());
        map.put("empireId", hasEmpire() ? getEmpire().getId().value() : null);
        map.put("settlementState", getState().name());
        map.put("finishedMissions", getFinishedMissions());
        map.put("missionPoints", getMissionPoints());
        map.put("residents", getResidentIds().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("outposts", getOutpostHomes().values().stream().collect(Collectors.toMap(NamedLocation::getName, NamedLocation::serialize)));
        map.put("missiondata", getMissionData().stream().collect(Collectors.toMap(md -> md.getMission().getDbId(), md -> md.getProgress())));
        return map;
    }

    public void transfer(Resident newChief) {
        if (isMember(newChief)) {
            Resident oldChief = this.getChief();
            Utils.callEvent(new LandownerChiefChangeEvent(this, this.getChief(), newChief));
            oldChief.setSettlementRank(SettlementRank.getChief().next());
            newChief.setSettlementRank(SettlementRank.getChief());
            setChief(newChief.getUUID());
            if (hasEmpire() && getEmpire().isChief(oldChief)) {
                oldChief.setEmpireRank(EmpireRank.getLeader().next());
                newChief.setEmpireRank(EmpireRank.getLeader());
            }
            DatabaseManager.getInstance().saveResident(oldChief);
            DatabaseManager.getInstance().saveResident(newChief);
            DatabaseManager.getInstance().saveSettlement(this);
        }
    }

    @Override
    public void removeMember(Targetable target) {
        if (target instanceof Resident && isMember((Resident) target)) {
            Utils.callEvent(new SettlementLeaveEvent(this, (Resident) target));
            residents.remove(((Resident) target).getUUID());
            SettlementManager.getInstance().leaveSettlement((Resident) target);
        }
    }

    public void addMember(Targetable target) {
        if (target instanceof Resident && !isMember((Resident) target)) {
            Resident resident = (Resident) target;
            Utils.callEvent(new SettlementJoinEvent(this, resident));
            resident.setSettlement(this);
            resident.setSettlementRank(SettlementRank.getDefault());
            resident.addChannel(ChatChannel.SETTLEMENT);
            resident.setDonated(0);
            resident.setTaxPaid(0);
            resident.setTaxExempted(false);
            if (Config.Settlement.Missions.DISTRIBUTE_ON_JOIN) {
                distributeNewMissions(resident);
            }
            resident.setTaxDebtAction(getTaxDebtAction());
            InviteManager.getInstance().removeInvite(resident, this);
            resident.save();
        }
    }

    @Override
    public String getTitle(Resident resident) {
        return resident.getSettlementTitle();
    }

    @Override
    public boolean setTitle(Resident resident, String title) {
        return resident.setSettlementTitle(title);
    }

    @Override
    public int getTitlesPerRank() {
        return Config.Settlement.TITLES_PER_RANK;
    }

    @Override
    public SettlementRank getHighestRank() {
        return SettlementRank.getChief();
    }

    @Override
    public SettlementRank getDefaultRank() {
        return SettlementRank.getDefault();
    }

    @Override
    public boolean useGuestRank() {
        return true;
    }

    @Override
    public SettlementRank getRank(Resident resident) {
        return isMember(resident) ? resident.getSettlementRank() : null;
    }

    @Override
    public Collection<SettlementRank> getRanks() {
        return RankManager.getInstance().getSettlementRanks();
    }

    @Override
    public Collection<OwnedLand> getLand() {
        return LandManager.getInstance().getClaimedLand(this);
    }

    @Override
    public long getTaxIncome() {
        long income = 0;
        for (Resident resident : getResidents()) {
            if (!resident.hasRankPermission("settlement.tax.exempt") && !resident.isTaxExempted()) {
                income += getTax();
            }
        }
        for (OwnedLand land : getLand()) {
            if (land.isPlot()) {
                Resident owner = ResidentManager.getInstance().getResident(land.getPlotOwner());
                if (owner != null && (!isMember(owner) || !owner.hasRankPermission("settlement.tax.exempt") || owner.isTaxExempted())) {
                    income += land.getTax();
                }
            }
        }
        return income;
    }

    @Override
    public long getMaxTax() {
        return Config.Settlement.MAX_TAX;
    }

    @Override
    public double getMaxAuctionTax() {
        return Config.Settlement.MAX_AUCTION_TAX;
    }

    public boolean leaveEmpire() {
        if (hasEmpire()) {
            getEmpire().removeMember(this);
            return true;
        }
        return false;
    }

    public boolean isCapital() {
        return hasEmpire() && getEmpire().isCapital(this);
    }

    public static Settlement deserialize(Map<String, Object> map) {
        SettlementsPlugin.logDebug("Creating settlement from " + map);
        Settlement settlement = LandOwner.deserialize(new Settlement(), map);
        if (settlement == null) {
            return null;
        }
        if (map == null || map.get("chief") == null || map.get("level") == null || map.get("settlementState") == null) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Settlement! (" + map + ")");
            return null;
        }
        settlement.setChief(UUID.fromString((String) map.get("chief")));
        settlement.setState(SettlementState.valueOf((String) map.get("settlementState")));
        settlement.setLevel((Integer) map.get("level"));
        settlement.setFinishedMissions(Utils.getLong(map.get("finishedMissions")));
        settlement.setMissionPoints(Utils.getLong(map.get("missionPoints")));
        settlement.setEmpire(EmpireManager.getInstance().getEmpireFromId(IdObject.get(map.get("empireId"))));
        ((Collection<String>) map.get("residents")).stream().map(UUID::fromString).forEach(u -> settlement.getResidentIds().add(u));
        ((Map<String, Map<String, Object>>) map.get("outposts")).values().stream()
                .map(NamedLocation::deserialize)
                .filter(Objects::nonNull)
                .forEach(settlement::setOutpostHome);
        ((Map<String, Object>) map.get("missiondata")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Map)
                .forEach(e -> {
                    Mission mission = Mission.fromDbId(e.getKey());
                    if (mission != null) {
                        MissionData md = new MissionData(mission);
                        for (Map.Entry<?, ?> o : ((Map<?, ?>) e.getValue()).entrySet()) {
                            if (o.getKey() instanceof String && o.getValue() instanceof Long) {
                                md.getProgress().put((String) o.getKey(), (Long) o.getValue());
                            }
                        }
                        settlement.addMissionData(md);
                    }
                });
        SettlementsPlugin.logDebug("Created settlement " + settlement.serialize());
        return settlement;
    }

    @Override
    public int compareTo(Settlement v) {
        if (v.getState() != getState())
            return getState().compareTo(v.getState());
        if (v.getLevel() != getLevel())
            return Integer.compare(v.getLevel(), getLevel());
        int otherClaimed = LandManager.getInstance().getClaimedLandSize(v);
        int claimed = LandManager.getInstance().getClaimedLandSize(this);
        if (otherClaimed != claimed)
            return Integer.compare(otherClaimed, claimed);
        if (v.getResidentIds().size() != getResidentIds().size())
            return Long.compare(v.getResidentIds().size(), getResidentIds().size());
        if (v.getBank() != getBank())
            return Long.compare(v.getBank(), getBank());
        return 0;
    }

    public String[] getReplacements(ConfigMessages configMessages, String... initial) {
        List<String> replacements = new ArrayList<>();
        Collections.addAll(replacements, initial);

        for (Placeholder replacement : Placeholder.values()) {
            replacements.add(replacement.name());
            replacements.add(replacement.getValue(configMessages, this));
        }

        return replacements.toArray(new String[0]);
    }

    public String getReplacement(ConfigMessages configMessages, String placeholder) {
        try {
            return Placeholder.valueOf(placeholder).getValue(configMessages, this);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public enum Placeholder {
        name((c, v) -> v.getName()),
        description((c, v) -> v.getDescription()),
        state((c, v) -> v.getStateTag()),
        level((c, v) -> String.valueOf(v.getLevel())),
        empire((c, v) -> v.hasEmpire() ? v.getEmpire().getName() : ""),
        empirelevel((c, v) -> v.hasEmpire() ? String.valueOf(v.getEmpire().getLevel()) : ""),
        access((c, v) -> v.getAccess().getTag()),
        jointype((c, v) -> v.getJoinType().getTag()),
        upkeep((c, v) -> Utils.CURRENCY_FORMAT.format(v.getUpkeep())),
        balance((c, v) -> Utils.withSuffix(v.getBank())),
        bank((c, v) -> Utils.CURRENCY_FORMAT.format(v.getBank())),
        joinfee((c, v) -> Utils.CURRENCY_FORMAT.format(v.getJoinFee())),
        tax((c, v) -> Utils.CURRENCY_FORMAT.format(v.getTax())),
        auctiontax((c, v) -> Utils.CURRENCY_FORMAT.format(v.getAuctionTax())),
        taxdebtaction((c, v) -> v.getTaxDebtAction().getTag()),
        missions((c, v) -> String.valueOf(v.getFinishedMissions())),
        missionpoints((c, v) -> String.valueOf(v.getMissionPoints())),
        extramissions((c, v) -> String.valueOf(v.getExtraMissions())),
        land((c, v) -> String.valueOf(LandManager.getInstance().getClaimedLandSize(v))),
        maxland((c, v) -> String.valueOf(v.getMaxClaimableChunks())),
        outposts((c, v) -> String.valueOf(v.getOutpostHomes().size())),
        maxoutposts((c, v) -> String.valueOf(v.getMaxOutposts())),
        abandoned((c, v) -> yesOrNo(v.isAbandoned())),
        abandonedcountdown((c, v) -> v.isAbandoned() ? UtilTime.format(v.getCreated() + TimeUnit.MINUTES.toMillis(Config.Settlement.Upkeep.ABANDON_TIME) - System.currentTimeMillis()) : ""),
        allowallyclaim((c, v) -> yesOrNo(v.isAllowAllyClaim())),
        hostilespawning((c, v) -> String.valueOf(v.getMode(PlotMode.mobSpawning))),
        passivespawning((c, v) -> String.valueOf(v.getMode(PlotMode.passiveSpawning))),
        firespread((c, v) -> String.valueOf(v.getMode(PlotMode.fireSpread))),
        explosions((c, v) -> String.valueOf(v.getMode(PlotMode.explosions))),
        pvp((c, v) -> String.valueOf(v.getMode(PlotMode.pvp))),
        buff((c, v) -> String.valueOf(v.getMode(PlotMode.buff))),
        bonusclaims((c, v) -> c != null
                ? v.getBonusClaimAmount() > 0 ? c.getMessage("bonus-claims").getReplacedText("amount", String.valueOf(v.getBonusClaimAmount())) : ""
                : String.valueOf(v.getBonusClaimAmount())),
        bonusoutposts((c, v) -> c != null
                ? v.getBonusOutposts() > 0 ? c.getMessage("bonus-outposts").getReplacedText("amount", String.valueOf(v.getBonusClaimAmount())) : ""
                : String.valueOf(v.getBonusOutposts())),
        bonus((c, v) -> String.valueOf(v.getBonusClaimAmount())),
        perks((c, v) -> v.getPerksMap().entrySet().stream()
                .map(p -> {
                    Perk perk = v.getPerk(p.getKey());
                    if (perk != null) {
                        return (p.getValue().getTimes() > 1 ? p.getValue().getTimes() + "x " : "") + perk.getTag();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "))),
        chiefrank((c, v) -> SettlementRank.getChief().getTag(v)),
        chieftitle((c, v) -> {
            Resident chief = v.getChief();
            return chief != null ? chief.getSettlementTitle() : "-";
        }),
        chiefdisplaytitle((c, v) -> {
            Resident chief = v.getChief();
            return chief != null ? chief.getDisplayTitle() : "-";
        }),
        chief((c, v) -> {
            Resident chief = v.getChief();
            return chief != null ? chief.getName() : "-";
        }),
        chiefonline((c, v) -> {
            Resident chief = v.getChief();
            return c != null ? (chief != null && chief.getPlayer() != null ? c.getText("residents.online") : c.getText("residents.offline")) : "";
        }),
        residents((c, v) -> c != null ? v.getResidentsToString(c.getMessage("residents.rank"), c.getMessage("residents.entry"), c.getMessage("residents.more"), c.getText("residents.online"), c.getText("residents.offline"), c.getText("residents.separator")) : ""),
        size((c, v) -> String.valueOf(v.getResidentIds().size())),
        maxsize((c, v) -> String.valueOf(v.getMaxSize())),
        resonlinecount((c, v) -> String.valueOf(v.getOnlineCount())),
        resotheronlinecount((c, v) -> String.valueOf(v.getOnlineCount() - 1)),
        officerranks((c, v) -> Arrays.stream(SettlementRank.values())
                .filter(r -> !r.isChief())
                .filter(r -> v.getEffectivePermissionValue(r, "settlement.officer").orElse(false))
                .map(r -> r.getTag(v))
                .collect(Collectors.joining(", "))),
        ;

        private final BiFunction<ConfigMessages, Settlement, String> valueProvider;

        Placeholder(BiFunction<ConfigMessages, Settlement, String> valueProvider) {
            this.valueProvider = valueProvider;
        }

        public String getValue(ConfigMessages configMessages, Settlement settlement) {
            return valueProvider.apply(configMessages, settlement);
        }
    }

    private String getResidentsToString(Message rankMessage, Message entry, Message more, String online, String offline, CharSequence separator) {
        Multimap<SettlementRank, Resident> rankMap = MultimapBuilder.treeKeys().arrayListValues().build();
        for (Resident resident : getResidents()) {
            rankMap.put(resident.getSettlementRank(), resident);
        }
        List<String> lines = new ArrayList<>();
        for (SettlementRank rank : rankMap.keySet()) {
            if (rank == SettlementRank.getChief()) {
                // should be listed extra
                continue;
            }
            String line = rankMessage.getReplacedText(
                    "rank", rank.getTag(this),
                    "residents", rankMap.get(rank).stream()
                            .sorted((r1, r2) -> {
                                Player p1 = r1.getPlayer();
                                Player p2 = r2.getPlayer();
                                if (p1 != null && p2 == null) {
                                    return -1;
                                } else if (p1 == null && p2 != null) {
                                    return 1;
                                }
                                return Long.compare(r1.getSettlementJoin(), r2.getSettlementJoin());
                            })
                            .limit(Config.PLAYERS_IN_INFO)
                            .map(res -> entry.getReplacedText(
                                    "online", res.getPlayer() != null ? online : offline,
                                    "name", res.getName(),
                                    "rank", rank.getTag(this),
                                    "title", res.getSettlementTitle()
                            ))
                            .collect(Collectors.joining(separator))
            );
            if (rankMap.get(rank).size() > Config.PLAYERS_IN_INFO) {
                line += more.getReplacedText(
                        "amount", String.valueOf(rankMap.get(rank).size()),
                        "more", String.valueOf(rankMap.get(rank).size() - Config.PLAYERS_IN_INFO)
                );
            }
            lines.add(line);
        }
        return String.join("\n", lines);
    }

    @Override
    public String name() {
        return "Settlement";
    }

    @Override
    public ItemStack getIcon() {
        Resident chief = getChief();
        return ItemUtils.getHead(chief.getUUID(), chief.getName());
    }

    @Override
    public Sort[] getSorts() {
        return Sort.values();
    }

    private enum Sort implements Sortable.Sort<Settlement> {
        LEVEL(Settlement::compareTo),
        SIZE((s1, s2) -> Integer.compare(s2.getResidentIds().size(), s1.getResidentIds().size())),
        NAME((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName())),
        PRIVACY((s1, s2) -> s1.getAccess().compareTo(s2.getAccess())),
        RECRUITMENT((s1, s2) -> s1.getJoinType().compareTo(s2.getJoinType())),
        AGE((s1, s2) -> Long.compare(s2.getCreated(), s1.getCreated())),
        ;

        private final Comparator<Settlement> comparator;

        Sort(Comparator<Settlement> comparator) {
            this.comparator = comparator;
        }

        @Override
        public Sort[] getValues() {
            return values();
        }

        @Override
        public Comparator<Settlement> getComparator() {
            return comparator;
        }
    }
}
