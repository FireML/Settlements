package de.themoep.settlements.empire;

import co.aikar.timings.lib.MCTiming;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.missions.MissionData;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.missions.MissionTaker;
import de.themoep.settlements.npc.NpcData;
import de.themoep.settlements.npc.NpcManager;
import de.themoep.settlements.perk.LevelPerk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.TaxDebtAction;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.perk.EmpirePerk;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.rank.RankManager;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.utils.Sortable;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

public class Empire extends LandOwner<EmpireRank, EmpireLevel> implements Comparable<Empire>, MissionProvider<EmpireMission> {
    private int level;
    private IdObject capital;
    private String symbol = "";
    private Set<IdObject> members = new LinkedHashSet<>();
    private boolean inRegen = false;
    private long lastRename = 0;
    private String worldName;
    private long finishedMissions = 0;

    protected Empire() {
        super();
    }

    public Empire(String name, IdObject capital, int level) {
        super(name);
        this.capital = capital;
        members.add(capital);
        this.level = level;
    }

    @Override
    public void save() {
        DatabaseManager.getInstance().saveEmpire(this);
    }

    @Override
    public void doUpkeep() {
        if (isAbandoned()) {
            SettlementsPlugin.getInstance().getLogger().log(Level.INFO, getStateTag() + " " + getName() + " is abandoned. No upkeep.");
            return;
        }

        long paidTaxes = 0;
        if (getTax() > 0) {
            long tax = getTax();
            for (Settlement member : getMembers()) {
                if (member.getId().equals(getCapitalId())) {
                    continue;
                }
                if (member.removeFromBank(tax)) {
                    addToBank(tax);
                    paidTaxes += tax;
                    Message.get("empire-tax.removed").tell(member.getOnlineResidents(),
                            "amount", String.valueOf(tax),
                            "state", member.getState().getTag(),
                            "settlement", member.getName(),
                            "name", getName());
                } else {
                    Message.get("empire-tax.debt-action." + getTaxDebtAction().getKey() + ".couldnt-pay").tell(getOnlineResidents(),
                            "amount", String.valueOf(tax),
                            "state", member.getState().getTag(),
                            "settlement", member.getName(),
                            "name", getName()
                    );
                    if (getTaxDebtAction() == TaxDebtAction.KICK) {
                        removeMember(member);
                    }
                }
            }
        }
        paidTaxes += doLandTaxes();
        payUpkeep(paidTaxes);

        if (paidTaxes > 0) {
            long finalPaidTaxes = paidTaxes;
            Utils.runAsync(() -> DatabaseManager.getInstance().log(this, null, null, "tax", "cash", finalPaidTaxes));
        }

        if (getTax() > 0) {
            DatabaseManager.getInstance().saveSettlements(getMembers());
        }

        if (distributeNewMissions() > -1) {
            DatabaseManager.getInstance().saveSettlementMissionData(getMembers());
        }

    }

    private long doLandTaxes() {
        long totalTax = 0;
        Set<OwnedLand> toUnclaim = new HashSet<>();
        for (OwnedLand land : getLand()) {
            if (land.isPlot() && land.getTax() > 0) {
                Resident resident = ResidentManager.getInstance().getResident(land.getPlotOwner());
                if (isChief(resident)) {
                    continue;
                }
                if (isMember(resident) && resident.hasRankPermission("empire.tax.exempt")) {
                    continue;
                }
                OfflinePlayer player = resident.getPlayer();
                if (player == null) {
                    player = Bukkit.getOfflinePlayer(resident.getUUID());
                }
                if (SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(player, land.getTax()).transactionSuccess()) {
                    addToBank(land.getTax());
                    totalTax += land.getTax();
                    if (player instanceof Player) {
                        Message.get("plot-tax.removed").tell((Player) player,
                                "amount", String.valueOf(land.getTax()),
                                "state", getStateTag(),
                                "name", getName(),
                                "x", String.valueOf(land.getX()),
                                "z", String.valueOf(land.getZ()),
                                "world", land.getWorldName()
                        );
                    }
                } else {
                    Message.get("plot-tax.debt-action." + getTaxDebtAction().getKey() + ".couldnt-pay").tell(getOnlineResidents(),
                            "player", resident.getName(),
                            "amount", String.valueOf(land.getTax()),
                            "state", getStateTag(),
                            "name", getName(),
                            "x", String.valueOf(land.getX()),
                            "z", String.valueOf(land.getZ()),
                            "world", land.getWorldName()
                    );
                    if (getTaxDebtAction() == TaxDebtAction.KICK && !land.containsHome() && !land.isOutpost()) {
                        toUnclaim.add(land);
                    }
                }
            }
        }
        LandManager.getInstance().unclaimLand(toUnclaim);
        return totalTax;
    }

    private void payUpkeep(long lastTax) {
        long total = getUpkeep();
        if (removeFromBank(total)) {
            SettlementsPlugin.getInstance().getLogger().log(Level.INFO, getStateTag() + " " + getName() + " was charged an upkeep of " + total + "!");
            MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.empire-alert.charged"), "charged", String.valueOf(total));
            if (!has(total - lastTax)) {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.cant-pay"), "amount", String.valueOf(total));
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveEmpire(this);
                DatabaseManager.getInstance().log(this, null, "upkeep", "cash", total);
            });
        } else {
            SettlementsPlugin.getInstance().getLogger().log(Level.INFO, getStateTag() + " " + getName() + " wasn't able to pay the upkeep of " + total + " and was abandoned!");
            setBank(0);
            if (Config.Empire.Upkeep.ABANDON_TIME > 0) {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.empire-alert.abandoned"));
                EmpireManager.getInstance().abandonEmpire(this);
            } else {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.empire-alert.deleted"));
                EmpireManager.getInstance().deleteEmpire(this);
            }
        }
    }

    @Override
    public int distributeNewMissions() {
        if (!hasUnlocked("missions")) {
            return -1;
        }
        int distributed = 0;
        for (Settlement member : getMembers()) {
            distributed += distributeNewMissions(member);
        }
        return distributed;
    }

    @Override
    public <T extends MissionTaker<EmpireMission>> int distributeNewMissions(T taker) {
        if (!hasUnlocked("missions")) {
            return 0;
        }
        taker.getMissionData().removeIf(md -> {
            if (md.isFinished()) {
                return Config.Empire.Missions.REMOVE_FINISHED_MISSIONS;
            }
            if (md.hasProgress()) {
                return Config.Empire.Missions.REMOVE_STARTED_MISSIONS;
            }
            return Config.Empire.Missions.REMOVE_NOT_STARTED_MISSIONS;
        });
        return distributeAdditionalMissions(taker);
    }

    @Override
    public int distributeAdditionalMissions() {
        if (!hasUnlocked("missions")) {
            return -1;
        }
        int distributed = 0;
        for (Settlement member : getMembers()) {
            distributed += distributeAdditionalMissions(member);
        }
        return distributed;
    }

    @Override
    public <T extends MissionTaker<EmpireMission>> int distributeAdditionalMissions(T taker) {
        if (!hasUnlocked("missions")) {
            return 0;
        }
        EmpireLevel levelInfo = getLevelInfo();
        if (levelInfo != null && levelInfo.getMissions() > 0) {
            int toDistribute = levelInfo.getMissions() + taker.getExtraMissions() - taker.getMissionData().size();
            if (toDistribute > 0) {
                List<EmpireMission> missions = new ArrayList<>(getMissions());
                Collections.shuffle(missions);
                int distributed = 0;
                for (int i = 0; i < toDistribute && i < missions.size(); i++) {
                    if (Config.Empire.Missions.DUPLICATE_MISSIONS) {
                        taker.addMissionData(new MissionData(missions.get(i)));
                        distributed++;
                    } else {
                        for (int j = i; j < missions.size(); j++) {
                            if (!taker.hasMission(missions.get(j))) {
                                taker.addMissionData(new MissionData(missions.get(j)));
                                distributed++;
                                break;
                            }
                        }
                    }
                }
                DatabaseManager.getInstance().saveMissionData(taker);
                return distributed;
            }
        }
        return 0;
    }

    @Override
    public boolean distributeMissionsOnLevelUp() {
        return Config.Empire.Missions.DISTRIBUTE_ON_LEVELUP;
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map = super.serialize();
        map.put("capitalId", getCapitalId().value());
        map.put("lastRename", getLastRename());
        map.put("symbol", getSymbol());
        map.put("level", getLevel());
        map.put("finishedMissions", getFinishedMissions());
        map.put("members", getMemberIds().stream().map(IdObject::value).filter(Objects::nonNull).collect(Collectors.toList()));
        return map;
    }

    @Override
    public void removeMember(Targetable target) {
        if (target instanceof Settlement && isMember((Settlement) target)) {
            members.remove(((Settlement) target).getId());
            EmpireManager.getInstance().leaveEmpire((Settlement) target);
        }
    }

    @Override
    public Ally removeAlly(LandOwner<?, ?> landOwner) {
        Ally ally = super.removeAlly(landOwner);
        if (ally != null) {
            for (Resident resident : landOwner.getResidents()) {
                Player player = resident.getPlayer();
                if (player != null) {
                    Empire e = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
                    if (e != null && !e.canAccess(resident)) {
                        Utils.teleportToSpawn(player);
                    }
                }
            }
            for (Resident resident : getResidents()) {
                Player player = resident.getPlayer();
                if (player != null) {
                    Empire e = EmpireManager.getInstance().getEmpireFromWorld(player.getWorld());
                    if (e != null && !e.canAccess(resident)) {
                        Utils.teleportToSpawn(player);
                    }
                }
            }
        }
        return ally;
    }


    @Override
    public Collection<OwnedLand> getLand() {
        return LandManager.getInstance().getClaimedLand(this);
    }

    @Override
    public Collection<? extends Perk> getAvailablePerks() {
        List<Perk> perks = new ArrayList<>();
        perks.addAll(EmpireManager.getInstance().getPerks());
        perks.addAll(NpcManager.getInstance().getPerks(this));
        return perks;
    }

    @Override
    public long getTaxIncome() {
        long income = 0;
        for (Settlement member : getMembers()) {
            if (!member.getId().equals(getCapitalId()) && member.has(getTax())) {
                income += getTax();
            }
        }
        for (OwnedLand land : getLand()) {
            if (land.isPlot()) {
                Resident owner = ResidentManager.getInstance().getResident(land.getPlotOwner());
                if (owner != null && (!isMember(owner) || !owner.hasPermission("empire.tax.exempt")) && !isCapital(owner.getSettlement())) {
                    income += land.getTax();
                }
            }
        }
        return income;
    }

    @Override
    public long getMaxTax() {
        return Config.Empire.MAX_TAX;
    }

    @Override
    public double getMaxAuctionTax() {
        return Config.Empire.MAX_AUCTION_TAX;
    }

    public static Empire deserialize(Map<String, Object> map) {
        SettlementsPlugin.logDebug("Creating empire from " + map);
        Empire empire = LandOwner.deserialize(new Empire(), map);
        if (empire == null) {
            return null;
        }
        if (map == null || map.get("capitalId") == null) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "Could not deserialize Empire! (" + map + ")");
            return null;
        }
        if (Config.Empire.World.RENAME) {
            empire.worldName = Config.Empire.World.PREFIX + empire.getName() + Config.Empire.World.SUFFIX;
        } else if (!empire.getId().isNull()) {
            empire.worldName = Config.Empire.World.PREFIX + "id" + empire.getId().value() + Config.Empire.World.SUFFIX;
        }
        empire.capital = IdObject.get(map.get("capitalId"));
        empire.lastRename = Utils.getLong(map.get("lastRename"));
        empire.setSymbol((String) map.get("symbol"));
        empire.setLevel((Integer) map.get("level"));
        empire.setFinishedMissions(Utils.getLong(map.get("finishedMissions")));
        if (map.get("members") != null && map.get("members") instanceof Collection) {
            for (Object memberId : (Collection) map.get("members")) {
                empire.members.add(IdObject.get(memberId));
            }
        }
        SettlementsPlugin.logDebug("Created empire " + empire.serialize());
        return empire;
    }

    @Override
    public int compareTo(Empire e) {
        if (isAbandoned()) {
            return -1;
        }
        if (e.getMemberIds().size() != getMemberIds().size())
            return Integer.compare(e.getMemberIds().size(), getMemberIds().size());
        return Long.compare(e.getBank(), getBank());
    }

    public int getBonusClaimAmount() {
        return getBonusClaims() + getPerksMap().entrySet().stream()
                .filter(e -> getPerk(e.getKey()) != null)
                .mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusClaims())
                .sum();
    }

    public int getBonusOutposts() {
        return getPerksMap().entrySet().stream()
                .filter(e -> getPerk(e.getKey()) != null)
                .mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusOutposts())
                .sum();
    }

    public int getBonusRadius() {
        return getPerksMap().entrySet().stream()
                .filter(e -> getPerk(e.getKey()) != null)
                .mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusRadius())
                .sum();
    }

    public int getWorldRadius() {
        return EmpireManager.getInstance().getLevel(level).map(EmpireLevel::getWorldRadius).orElse(500) + getBonusRadius();
    }

    public Long getClaimUpkeepReduction() {
        return EmpireManager.getInstance().getLevel(level).map(EmpireLevel::getUpkeepReduction).orElse(0L);
    }

    @Override
    public boolean isMaxLevel() {
        return (getLevel() >= Config.Empire.Leveling.MAX_LEVEL) || getNextLevelRequirements().isEmpty();
    }

    @Override
    public boolean checkLevelUp(Resident resident) {
        return EmpireManager.getInstance().checkLevelUp(this, resident);
    }

    @Override
    public Collection<? extends LevelPerk> getPerks(int level) {
        return EmpireManager.getInstance().getPerks().stream().filter(p -> p.getLevel() == level).collect(Collectors.toList());
    }

    @Override
    public long getAmount(String key) {
        if (key.equalsIgnoreCase("world")) {
            return getWorldRadius();
        }
        if (key.equalsIgnoreCase("level")) {
            return getLevel();
        }
        if (key.equalsIgnoreCase("missions")) {
            return finishedMissions;
        }
        return super.getAmount(key);
    }

    @Override
    public void setAmount(String key, long amount) {
        if ("missions".equalsIgnoreCase(key)) {
            setFinishedMissions(amount);
        } else {
            super.setAmount(key, amount);
        }
    }

    public Settlement getCapital() {
        return SettlementManager.getInstance().getFromId(capital);
    }

    public IdObject getCapitalId() {
        return capital;
    }

    public void setCapital(Settlement capital) {
        this.capital = capital != null ? capital.getId() : IdObject.get(null);
    }

    @Override
    public EmpirePerk getPerk(String name) {
        return EmpireManager.getInstance().getPerk(name);
    }

    @EventHandler
    public boolean isUnlockable(String unlockable) {
        return EmpireManager.getInstance().getUnlockables().contains(unlockable);
    }

    @Override
    public void apply(Player player, Perk perk) {
        super.apply(player, perk);
        if (perk instanceof EmpirePerk && ((EmpirePerk) perk).getBonusRadius() > 0) {
            World world = hasWorld() ? Bukkit.getWorld(getWorldName()) : null;
            if (world != null) {
                world.getWorldBorder().setSize(getWorldRadius() * 2, 5);
            } else if (Config.Empire.World.ENABLED && !hasWorld() && getWorldRadius() > 0) {
                if (Config.Empire.World.RENAME) {
                    setWorldName(Config.Empire.World.PREFIX + getName() + Config.Empire.World.SUFFIX);
                } else {
                    setWorldName(Config.Empire.World.PREFIX + "id" + getId().value() + Config.Empire.World.SUFFIX);
                }
                Utils.runSync(() -> {
                    try (MCTiming ignored = SettlementsPlugin.getInstance().getAndStartTiming("Empire world creation")) {
                        EmpireManager.getInstance().createEmpireWorld(this);
                        Message.get("empire-world-generated").tell(this);
                    }
                });
            }
        }
    }

    @Override
    public Set<String> getPermissions() {
        return RankManager.getInstance().getEmpireLeaderRank().getInheritedPermissions();
    }

    @Override
    public Collection<String> getToggleablePermissions() {
        return RankManager.getInstance().getEmpireToggleablePerms();
    }

    @Override
    public BuildState getDefaultBuildState() {
        return BuildState.PRIVATE;
    }

    @Override
    public BuildState.AppliesTo getBuildStateType() {
        return BuildState.AppliesTo.EMPIRE;
    }

    public Set<IdObject> getMemberIds() {
        return members;
    }

    public Collection<Settlement> getMembers() {
        return getMemberIds().stream().map(i -> SettlementManager.getInstance().getFromId(i)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean isFull() {
        EmpireLevel level = getLevelInfo();
        if (level != null) {
            int levelSize = level.getSize();
            if (levelSize > 0) {
                return getMemberIds().size() >= levelSize;
            }
        }
        return Config.Empire.MAX_SIZE > 0 && getMemberIds().size() >= Config.Empire.MAX_SIZE;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
        if (!ConfigManager.getInstance().getDataConfig().getConfig().contains("empire.level" + level)) {
            ConfigManager.getInstance().getDataConfig().getConfig().set("empire.level" + level, getName());
            ConfigManager.getInstance().getDataConfig().saveConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                MessageUtils.tell(player, Message.get("first-to-reach-level"),
                        "name", this.getName(),
                        "state", "empire",
                        "level", String.valueOf(level));
            }
        }
    }

    @Override
    @Nullable
    public EmpireLevel getLevelInfo() {
        return EmpireManager.getInstance().getLevel(getLevel()).orElse(null);
    }

    @Override
    public Optional<EmpireLevel> getNextLevel() {
        return EmpireManager.getInstance().getLevel(getLevel() + 1);
    }

    @Override
    public boolean isChief(UUID playerId) {
        return getCapital().isChief(playerId);
    }

    @Override
    public UUID getChiefId() {
        return getCapital().getChiefId();
    }

    @Override
    public Resident getChief() {
        return getCapital().getChief();
    }

    @Override
    public boolean isChief(Targetable target) {
        if (super.isChief(target)) {
            return true;
        }
        if (target instanceof Settlement) {
            return isCapital((Settlement) target);
        }
        return false;
    }

    @Override
    public String getTitle(Resident resident) {
        return resident.getEmpireTitle();
    }

    @Override
    public boolean setTitle(Resident resident, String title) {
        return resident.setEmpireTitle(title);
    }

    @Override
    public int getTitlesPerRank() {
        return Config.Empire.TITLES_PER_RANK;
    }

    @Override
    public EmpireRank getHighestRank() {
        return EmpireRank.getLeader();
    }

    @Override
    public EmpireRank getDefaultRank() {
        return EmpireRank.getDefault();
    }

    @Override
    public boolean useGuestRank() {
        return false;
    }

    @Override
    public EmpireRank getRank(Resident resident) {
        return isMember(resident) ? resident.getEmpireRank() : null;
    }

    @Override
    public Collection<EmpireRank> getRanks() {
        return RankManager.getInstance().getEmpireRanks();
    }

    @Override
    public boolean isOfficer(Player player) {
        Resident res = ResidentManager.getInstance().getResident(player);
        return hasPermission(res, "officer");
    }

    public boolean isCapital(Settlement settlement) {
        return settlement != null && capital.equals(settlement.getId());
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getFinishedMissions() {
        return finishedMissions;
    }

    public void setFinishedMissions(long finishedMissions) {
        this.finishedMissions = finishedMissions;
    }

    @Override
    public Collection<EmpireMission> getMissions() {
        return EmpireManager.getInstance().getMissions();
    }

    @Override
    public long getMissionRedistributePrice() {
        return Config.Empire.Missions.REDISTRIBUTE_PRICE;
    }

    @Override
    public List<Resident> getResidents() {
        return getMembers().stream().flatMap(v -> v.getResidents().stream()).collect(Collectors.toList());
    }

    @Override
    public long getOnlineCount() {
        return getMembers().stream().mapToLong(Settlement::getOnlineCount).sum();
    }

    @Override
    public List<Resident> getOnlineResidents() {
        return getMembers().stream().flatMap(v -> v.getOnlineResidents().stream()).collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(Resident resident, String perm) {
        return isMember(resident) && resident.hasPermission(perm.startsWith("empire.") ? perm : "empire." + perm);
    }

    @Override
    public boolean isMember(Resident resident) {
        return isMember(resident.getSettlement());
    }

    @Override
    public String getStateKey() {
        return State.EMPIRE.getKey();
    }

    @Override
    public String getStateTag() {
        return State.EMPIRE.getTag();
    }

    @Override
    public MissionTaker<EmpireMission> getMissionTaker(Resident resident) {
        return isMember(resident) ? resident.getSettlement() : null;
    }

    @Override
    public Resident getResident(String name) {
        for (Settlement member : getMembers()) {
            for (Resident resident : member.getResidents()) {
                if (resident.getName().equalsIgnoreCase(name)) {
                    return resident;
                }
            }
        }
        return null;
    }

    @Override
    public long getUpkeep() {
        long amount = EmpireManager.getInstance().getLevel(level).map(EmpireLevel::getUpkeep).orElse(0L);

        for (OwnedLand land : LandManager.getInstance().getClaimedLand(this)) {
            amount += getClaimUpkeep(land.getWorld());
        }
        for (NpcData data : getNpcData()) {
            amount += data.getLevelInfo().getUpkeep();
        }
        return amount;
    }

    @Override
    public long getClaimUpkeep(String world) {
        if (hasWorld() && world.equalsIgnoreCase(getWorldName())) {
            return 0;
        }
        return Math.max(0, EmpireManager.getInstance().getLevel(getLevel()).map(l -> l.getClaimUpkeep(world)).orElse(0L));
    }

    @Override
    public boolean isAbandoned() {
        return members.isEmpty();
    }

    @Override
    public long getAbandonTimeConfig() {
        return Config.Empire.Upkeep.ABANDON_TIME;
    }

    @Override
    public boolean canClaim(World world) {
        return !Config.Empire.Claiming.DISABLED_WORLDS.contains(world.getName())
                && !Config.Claiming.DISABLED_WORLDS.contains(world.getName())
                && EmpireManager.getInstance().getEmpireFromWorld(world) == null;
    }

    @Override
    public int getMaxClaimableChunks() {
        return EmpireManager.getInstance().getLevel(getLevel()).map(EmpireLevel::getChunks).orElse(0)
                + getBonusClaimAmount();
    }

    @Override
    public int getMaxSize() {
        int maxSize = getLevelInfo() != null ? getLevelInfo().getSize() : -1;
        return maxSize > 0 ? maxSize : Config.Empire.MAX_SIZE;
    }

    public void setInRegen(boolean inRegen) {
        lastRename = System.currentTimeMillis();
        this.inRegen = inRegen;
    }

    public boolean isInRegen() {
        return inRegen;
    }

    public long getLastRename() {
        return lastRename;
    }

    @Override
    public String name() {
        return "Empire";
    }

    public boolean hasWorld() {
        return worldName != null && getWorldRadius() > 0;
    }

    private enum State implements NameTag {
        EMPIRE;
    }

    public String getWorldName() {
        return worldName;
    }

    void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public boolean isMember(Settlement settlement) {
        return settlement != null && equals(settlement.getEmpire());
    }

    @Override
    public boolean isTrusted(LandOwner landOwner) {
        return landOwner instanceof Settlement && isMember((Settlement) landOwner);
    }

    @Override
    public boolean setMode(PlotMode mode, boolean value) {
        if (super.setMode(mode, value)) {
            if (SettlementsPlugin.getInstance().isRunning() && mode == PlotMode.mobSpawning && !value && hasWorld()) {
                World world = Bukkit.getWorld(getWorldName());
                if (world != null) {
                    try (MCTiming ignored = SettlementsPlugin.getInstance().getAndStartTiming("Empire mob spawning mode toggle")) {
                        for (LivingEntity entity : world.getLivingEntities()) {
                            if (Utils.isHostile(entity)) {
                                entity.remove();
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean canAccess(Player player) {
        return canAccess(ResidentManager.getInstance().getResident(player));
    }

    public boolean canAccess(Resident resident) {
        if (getAccess().atLeast(LandAccess.PUBLIC)) {
            return true;
        }
        if (getAccess().atLeast(LandAccess.TRUSTED)) {
            return isTrusted(resident);
        }
        if (getAccess().atLeast(LandAccess.ALLY)) {
            return isTrusted(resident) || isAlly(resident);
        }
        if (getAccess().atLeast(LandAccess.PRIVATE)) {
            return getCapital().isMember(resident);
        }
        return false;
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
        name((c, e) -> e.getName()),
        description((c, e) -> e.getDescription()),
        state((c, e) -> e.getStateTag()),
        symbol((c, e) -> e.getSymbol()),
        level((c, e) -> String.valueOf(e.getLevel())),
        access((c, e) -> e.getAccess().getTag()),
        jointype((c, v) -> v.getJoinType().getTag()),
        upkeep((c, e) -> Utils.CURRENCY_FORMAT.format(e.getUpkeep())),
        balance((c, e) -> Utils.withSuffix(e.getBank())),
        bank((c, e) -> Utils.CURRENCY_FORMAT.format(e.getBank())),
        joinfee((c, e) -> Utils.CURRENCY_FORMAT.format(e.getJoinFee())),
        tax((c, e) -> Utils.CURRENCY_FORMAT.format(e.getTax())),
        auctiontax((c, e) -> Utils.toPercentage(e.getAuctionTax())),
        taxdebtaction((c, e) -> e.getTaxDebtAction().getTag()),
        dailymissions((c, e) -> String.valueOf(e.getLevelInfo() != null ? e.getLevelInfo().getMissions() : 0)),
        missions((c, e) -> String.valueOf(e.finishedMissions)),
        abandoned((c, e) -> yesOrNo(e.isAbandoned())),
        abandonedcountdown((c, e) -> e.isAbandoned() ? UtilTime.format(e.getCreated() + TimeUnit.MINUTES.toMillis(Config.Empire.Upkeep.ABANDON_TIME) - System.currentTimeMillis()) : ""),
        hostilespawning((c, e) -> String.valueOf(e.getMode(PlotMode.mobSpawning))),
        passivespawning((c, e) -> String.valueOf(e.getMode(PlotMode.passiveSpawning))),
        firespread((c, e) -> String.valueOf(e.getMode(PlotMode.fireSpread))),
        explosions((c, e) -> String.valueOf(e.getMode(PlotMode.explosions))),
        pvp((c, e) -> String.valueOf(e.getMode(PlotMode.pvp))),
        buff((c, e) -> String.valueOf(e.getMode(PlotMode.buff))),
        bonusoutposts((c, e) -> String.valueOf(e.getBonusOutposts())),
        bonusclaims((c, e) -> String.valueOf(e.getBonusClaimAmount())),
        bonus((c, e) -> String.valueOf(e.getBonusClaimAmount())),
        capital((c, e) -> {
            Settlement capital = e.getCapital();
            return capital != null ? capital.getName() : "-";
        }),
        capitalstate((c, e) -> {
            Settlement capital = e.getCapital();
            return capital != null ? capital.getStateTag() : "-";
        }),
        perks((c, e) -> e.getPerksMap().entrySet().stream()
                .map(p -> (p.getValue().getTimes() > 1 ? p.getValue().getTimes() + "x " : "") + e.getPerk(p.getKey()).getTag())
                .collect(Collectors.joining(", "))),
        chiefrank((c, e) -> {
            Settlement capital = e.getCapital();
            return SettlementRank.getChief().getTag(capital);
        }),
        chieftitle((c, e) -> {
            Settlement capital = e.getCapital();
            Resident chief = capital != null ? capital.getChief() : null;
            return chief != null ? chief.getSettlementTitle() : "-";
        }),
        chiefsettlementtitle((c, e) -> {
            Settlement capital = e.getCapital();
            Resident chief = capital != null ? capital.getChief() : null;
            return chief != null ? chief.getSettlementTitle() : "-";
        }),
        chiefempiretitle((c, e) -> {
            Settlement capital = e.getCapital();
            Resident chief = capital != null ? capital.getChief() : null;
            return chief != null ? chief.getEmpireTitle() : "-";
        }),
        chiefdisplaytitle((c, e) -> {
            Settlement capital = e.getCapital();
            Resident chief = capital != null ? capital.getChief() : null;
            return chief != null ? chief.getDisplayTitle() : "-";
        }),
        chief((c, e) -> {
            Settlement capital = e.getCapital();
            Resident chief = capital != null ? capital.getChief() : null;
            return chief != null ? chief.getName() : "-";
        }),
        chiefonline((c, e) -> {
            Settlement capital = e.getCapital();
            Resident chief = capital != null ? capital.getChief() : null;
            return c != null ? (chief != null && chief.getPlayer() != null ? c.getText("special-residents.online") : c.getText("special-residents.offline")) : "";
        }),
        specialresidents((c, e) -> c != null ? e.getResidentsToString(c.getMessage("special-residents.rank"), c.getMessage("special-residents.entry"), c.getText("special-residents.online"), c.getText("special-residents.offline"), c.getText("special-residents.separator")) : ""),
        size((c, e) -> String.valueOf(e.getMembers().size())),
        members((c, e) -> c != null ? e.getMembersToString(c) : ""),
        resonlinecount((c, e) -> String.valueOf(e.getOnlineCount())),
        resotheronlinecount((c, e) -> String.valueOf(e.getOnlineCount() - 1)),
        officerranks((c, e) -> Arrays.stream(EmpireRank.values())
                .filter(r -> r != EmpireRank.getLeader())
                .filter(r -> e.getEffectivePermissionValue(r, "empire.officer").orElse(false))
                .map(NameTag::getTag)
                .collect(Collectors.joining(", "))),
        ;

        private final BiFunction<ConfigMessages, Empire, String> valueProvider;

        Placeholder(BiFunction<ConfigMessages, Empire, String> valueProvider) {
            this.valueProvider = valueProvider;
        }

        public String getValue(ConfigMessages c, Empire e) {
            return valueProvider.apply(c, e);
        }
    }

    private String getMembersToString(ConfigMessages configMessages) {
        if (members.isEmpty()) {
            return "";
        }
        Multimap<SettlementState, Settlement> members = MultimapBuilder.enumKeys(SettlementState.class).arrayListValues().build();
        for (Settlement member : getMembers()) {
            members.put(member.getState(), member);
        }
        Message stateMessage = configMessages.getMessage("members.state");
        Message entryMessage = configMessages.getMessage("members.entry");
        String separator = configMessages.getMessage("members.separator").getText();
        List<String> lines = new ArrayList<>();
        for (SettlementState state : SettlementState.values()) {
            if (!members.containsKey(state)) {
                continue;
            }
            lines.add(stateMessage.getReplacedText(
                    "state", state.getTag(),
                    "members", members.get(state).stream()
                            .sorted()
                            .map(v -> entryMessage.getReplacedText(
                                    "state", v.getStateTag(),
                                    "name", v.getName(),
                                    "level", String.valueOf(v.getLevel()),
                                    "residents", String.valueOf(v.getResidentIds().size())
                            )).collect(Collectors.joining(separator))
            ));
        }

        return String.join("\n", lines);
    }

    private String getResidentsToString(Message rankMessage, Message entry, String online, String offline, CharSequence separator) {
        Multimap<EmpireRank, Resident> rankMap = MultimapBuilder.treeKeys().arrayListValues().build();
        for (Resident resident : getResidents()) {
            rankMap.put(resident.getEmpireRank(), resident);
        }
        List<String> lines = new ArrayList<>();
        for (EmpireRank rank : rankMap.keySet()) {
            if (rank == EmpireRank.getLeader() || rank == EmpireRank.getDefault()) {
                // leader is listed extra, default rank is not displayed
                continue;
            }
            lines.add(rankMessage.getReplacedText(
                    "rank", rank.getTag(),
                    "residents", rankMap.get(rank).stream()
                            .sorted((r1, r2) -> {
                                Player p1 = r1.getPlayer();
                                Player p2 = r2.getPlayer();
                                if (p1 != null && p2 == null) {
                                    return -1;
                                } else if (p1 == null && p2 != null) {
                                    return 1;
                                }
                                return Long.compare(r1.getLastLogin(), r2.getLastLogin());
                            })
                            .limit(Config.PLAYERS_IN_INFO)
                            .map(res -> entry.getReplacedText(
                                    "online", res.getPlayer() != null ? online : offline,
                                    "name", res.getName(),
                                    "rank", rank.getTag(),
                                    "title", res.getSettlementTitle(),
                                    "state", res.getSettlement().getStateTag(),
                                    "settlement", res.getSettlement().getName()
                            ))
                            .collect(Collectors.joining(separator))
            ));
        }
        return String.join("\n", lines);
    }

    @Override
    public Sort[] getSorts() {
        return Sort.values();
    }

    @Override
    public ItemStack getIcon() {
        Resident chief = getCapital().getChief();
        return ItemUtils.getHead(chief.getUUID(), chief.getName());
    }

    private enum Sort implements Sortable.Sort<Empire> {
        LEVEL(Empire::compareTo),
        SIZE((e1, e2) -> Integer.compare(e2.getMemberIds().size(), e1.getMemberIds().size())),
        NAME((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName())),
        PRIVACY((e1, e2) -> e1.getAccess().compareTo(e2.getAccess())),
        RECRUITMENT((e1, e2) -> e1.getJoinType().compareTo(e2.getJoinType())),
        AGE((e1, e2) -> Long.compare(e2.getCreated(), e1.getCreated())),
        ;

        private final Comparator<Empire> comparator;

        Sort(Comparator<Empire> comparator) {
            this.comparator = comparator;
        }

        @Override
        public Sort[] getValues() {
            return values();
        }

        @Override
        public Comparator<Empire> getComparator() {
            return comparator;
        }
    }
}
