package de.themoep.settlements.settlement;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.AllyRequest;
import de.themoep.settlements.land.Banished;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.FlagAble;
import de.themoep.settlements.land.JoinRequest;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.land.LandManager;

import de.themoep.settlements.land.ModeAble;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.TaxDebtAction;
import de.themoep.settlements.land.Unbanished;
import de.themoep.settlements.land.Use;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.land.events.SettlementChiefChangeEvent;
import de.themoep.settlements.missions.MissionData;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.perk.SettlementPerk;
import de.themoep.settlements.perk.Vaultable;
import de.themoep.settlements.perk.events.PerkUnlockedEvent;
import de.themoep.settlements.rank.RankManager;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.events.SettlementJoinEvent;
import de.themoep.settlements.settlement.events.SettlementLeaveEvent;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Sortable;
import de.themoep.settlements.utils.StringUtils;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.UtilTime;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigManager;
import de.themoep.settlements.world.NamedLocation;
import de.themoep.settlements.world.SimpleLocation;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.themoep.settlements.utils.MessageUtils.yesOrNo;

public class Settlement implements Comparable<Settlement>, Targetable, Sortable<Settlement>, de.themoep.settlements.db.DatabaseSerializable, de.themoep.settlements.land.ModeAble, de.themoep.settlements.land.FlagAble, de.themoep.settlements.perk.Vaultable, de.themoep.settlements.utils.NameTag {
    private final Map<PlotMode, Boolean> modes = new EnumMap<>(PlotMode.class);
    private final Map<String, Long> vault = new HashMap<>();
    private final Map<String, PerkData> perks = new LinkedHashMap<>();
    private final Map<IdObject, JoinRequest<?>> joinRequests = new HashMap<>();
    private final Map<Use, BuildState> use = new EnumMap<>(Use.class);
    private final Map<String, Map<String, Boolean>> ranksPermissions = new HashMap<>();
    private final Table<String, IdObject, AllyRequest> allyRequests = HashBasedTable.create();
    private final List<Unbanished> unbanished = new ArrayList<>();
    protected boolean allowAllyClaim = false;
    protected Multimap<String, String> rankTitles = MultimapBuilder.hashKeys().linkedHashSetValues().build();
    protected Map<String, String> defaultRankTitles = new HashMap<>();
    protected Table<String, IdObject, Ally> allies = HashBasedTable.create();
    protected Table<String, IdObject, Banished> banished = HashBasedTable.create();
    private int level;
    private UUID chief;
    private SettlementState settlementState;
    private final Map<String, NamedLocation> outpostHomes = new LinkedHashMap<>();
    private final Set<UUID> residents = new LinkedHashSet<>();
    private long missionPoints = 0;
    private long finishedMissions = 0;
    private IdObject id = IdObject.get(null);
    private String name;
    private String description = "";
    private String rules = "";
    private String discordLink = "";
    private long created = System.currentTimeMillis();
    private long bank = 0;
    private long joinFee = 0;
    private long tax = 0;
    private double auctionTax = 0;
    private TaxDebtAction taxDebtAction = TaxDebtAction.KICK;
    private int bonusClaims = 0;
    private SimpleLocation home = null;
    private BuildState buildState = getDefaultBuildState();
    private LandAccess access = LandAccess.PRIVATE;
    private JoinType joinType = JoinType.INVITE_ONLY;

    protected Settlement() {
        super();
    }

    public Settlement(String name, UUID chief, int level, SettlementState state) {
        this.name = name;
        residents.add(chief);
        this.chief = chief;
        this.level = level;
        this.settlementState = state;
    }

    public static Settlement deserialize(Settlement landOwner, Map<String, Object> map) {
        if (map == null || map.get("name") == null || map.get("buildState") == null) {
            SettlementsPlugin.getInstance().getLogging().warn("Could not deserialize LandOwner! (" + map + ")");
            return null;
        }
        landOwner.setName((String) map.get("name"));
        landOwner.setDescription((String) map.get("description"));
        landOwner.setRules((String) map.get("rules"));
        landOwner.setDiscord((String) map.get("discord"));
        landOwner.setCreated(Utils.getLong(map.get("created")));
        landOwner.setId(IdObject.get(map));
        landOwner.setBank(Utils.getLong(map.get("bank")));
        landOwner.setJoinFee(Utils.getLong(map.get("joinFee")));
        //noinspection rawtypes,unchecked
        for (JoinRequest<?> request : ((Collection<JoinRequest>) map.get("joinRequests"))) {
            landOwner.addJoinRequest(request);
        }
        landOwner.setTax(Utils.getLong(map.get("tax")));
        landOwner.setAuctionTax(Utils.getDouble(map.get("auctionTax")));
        try {
            landOwner.setTaxDebtAction(TaxDebtAction.valueOf((String) map.get("taxDebtAction")));
        } catch (IllegalArgumentException ignored) {}
        for (PlotMode mode : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            landOwner.setMode(mode, Utils.getBoolean(map.getOrDefault(mode.name(), mode.getDefault())));
        }
        landOwner.setBonusClaims((Integer) map.get("bonusClaims"));
        try {
            landOwner.setBuildState(BuildState.valueOf((String) map.get("buildState")));
        } catch (IllegalArgumentException ignored) {}
        try {
            landOwner.setAccess(LandAccess.valueOf((String) map.get("access")));
        } catch (IllegalArgumentException ignored) {}
        try {
            landOwner.setJoinType(JoinType.valueOf((String) map.get("joinType")));
        } catch (IllegalArgumentException ignored) {}
        if (map.get("homeLoc") != null) {
            landOwner.setHome(SimpleLocation.deserialize((Map<String, Object>) map.get("homeLoc")));
        }
        Map<String, Object> uses = (Map<String, Object>) map.get("uses");
        for (Use u : Use.values()) {
            UseFlag useFlag = UseFlagManager.getInstance().get(u);
            if (uses.containsKey(u.name()) && useFlag != null) {
                landOwner.setUseState(u, Utils.parseEnum(BuildState.class, (String) uses.get(u.name()), useFlag.getDefault(landOwner.getDefaultBuildState())));
            }
        }
        for (Ally ally : ((Collection<Ally>) map.get("allies"))) {
            landOwner.allies.put(ally.getType(), ally.getLandOwnerId(), ally);
        }
        for (AllyRequest request : ((Collection<AllyRequest>) map.get("allyRequests"))) {
            landOwner.addAllyRequest(request);
        }
        landOwner.setAllowAllyClaim(Utils.getBoolean(map.get("allowAllyClaim")));

        for (Banished banished : ((Collection<Banished>) map.get("banished"))) {
            landOwner.banished.put(banished.type(), banished.banishedId(), banished);
        }
        landOwner.getUnbanished().addAll(((Collection<Unbanished>) map.get("unbanished")));
        ((Map<String, Object>) map.get("vault")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Number)
                .forEach(e -> landOwner.getVault().put(e.getKey(), Long.parseLong(String.valueOf(e.getValue()))));
        ((Map<String, Object>) map.get("perks")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Map)
                .forEach(e -> landOwner.getPerksMap().put(e.getKey().toLowerCase(), PerkData.deserialize((Map<String, Object>) e.getValue())));
        ((Map<String, Object>) map.get("permissions")).entrySet().stream()
                .filter(e -> e.getValue() instanceof Map)
                .forEach(e -> landOwner.getRanksPermissions().put(e.getKey().toLowerCase(), (Map<String, Boolean>) e.getValue()));
        for (Map.Entry<String, Collection<String>> entry : ((Map<String, Collection<String>>) map.get("titles")).entrySet()) {
            landOwner.rankTitles.putAll(entry.getKey().toLowerCase(), entry.getValue());
        }
        ((Map<String, Object>) map.get("defaulttitles")).forEach((key, value) -> landOwner.defaultRankTitles.put(key.toLowerCase(), (String) value));
        return landOwner;
    }

    @Override
    public void save() {
        DatabaseManager.getInstance().saveSettlement(this);
    }

    public void doUpkeep() {
        if (isAbandoned()) {
            SettlementsPlugin.getInstance().getLogging().info(getStateTag() + " " + getName() + " is abandoned. No upkeep.");
            return;
        }
        int inactiveDays = Config.Settlement.Upkeep.INACTIVE_TIME;
        if (getState() == SettlementState.VILLAGE && getLevel() == 1 && System.currentTimeMillis() - getCreated() >= TimeUnit.DAYS.toMillis(inactiveDays)) {
            SettlementManager.getInstance().abandon(this);
            return;
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
            taxes.merge(entry.getKey(), entry.getValue(), Long::sum);
        }

        long totalTax = 0;
        for (Map.Entry<Resident, Long> entry : taxes.entrySet()) {
            Resident resident = entry.getKey();
            long tax = entry.getValue();
            OfflinePlayer player = resident.getPlayer();
            if (player == null) {
                player = Bukkit.getOfflinePlayer(resident.getUUID());
            }
            EconomyResponse response = SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(player, tax);
            if (response.transactionSuccess()) {
                resident.addTaxPaid(tax);
                resident.save();
                addToBank(tax);
                totalTax += tax;
                if (player instanceof Player pl) {
                    Message.get("tax.removed").tell(pl,
                            "amount", String.valueOf(tax),
                            "state", getState().getTag(),
                            "name", getName());
                }
            } else if (!SettlementsPlugin.getInstance().getEconomy().has(player, tax)
                    && (player.getName() != null || !SettlementsPlugin.getInstance().getEconomy().has(resident.getPlayer(), tax))) {
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
            } else {
                SettlementsPlugin.getInstance().getLogging().warn("Error while trying to withdraw " + tax + " tax from " + resident.getName() + ": " + response.errorMessage);
            }
        }

        if (totalTax > 0) {
            long finalTotalTax = totalTax;
            Utils.runAsync(() -> DatabaseManager.getInstance().log(this, null, "tax", "cash", finalTotalTax));
        }
        payUpkeep(totalTax);

        if (distributeNewMissions() > -1) {
            DatabaseManager.getInstance().saveResidentMissionData(getResidents());
        }
    }

    public int distributeNewMissions() {
        if (!hasUnlocked("missions")) {
            return -1;
        }
        int distributed = 0;
        for (Resident resident : getResidents()) {
            distributed += distributeNewMissions(resident);
        }
        return distributed;
    }

    public int distributeNewMissions(Resident taker) {
        if (!hasUnlocked("missions")) {
            return 0;
        }
        taker.getMissionData().removeIf(md -> {
            if (md.isFinished()) {
                return Config.Settlement.Missions.REMOVE_FINISHED_MISSIONS;
            }
            if (md.hasProgress()) {
                return Config.Settlement.Missions.REMOVE_STARTED_MISSIONS;
            }
            return Config.Settlement.Missions.REMOVE_NOT_STARTED_MISSIONS;
        });
        return distributeAdditionalMissions(taker);
    }

    public int distributeAdditionalMissions() {
        if (!hasUnlocked("missions")) {
            return -1;
        }
        int distributed = 0;
        for (Resident resident : getResidents()) {
            distributed += distributeAdditionalMissions(resident);
        }
        return distributed;
    }

    public int distributeAdditionalMissions(Resident taker) {
        if (!hasUnlocked("missions")) {
            return 0;
        }
        SettlementLevel levelInfo = getLevelInfo();
        if (levelInfo != null && levelInfo.getMissions() > 0) {
            int toDistribute = levelInfo.getMissions() + taker.getExtraMissions() - taker.getMissionData().size();
            int distributed = 0;
            if (toDistribute > 0) {
                List<SettlementMission> missions = new ArrayList<>(getMissions());
                Collections.shuffle(missions);
                for (int i = 0; i < toDistribute && i < missions.size(); i++) {
                    if (Config.Settlement.Missions.DUPLICATE_MISSIONS) {
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
            }
            return distributed;
        }
        return 0;
    }

    public boolean distributeMissionsOnLevelUp() {
        return Config.Settlement.Missions.DISTRIBUTE_ON_LEVELUP;
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
                taxes.put(resident, taxes.computeIfAbsent(resident, _ -> 0L) + land.getTax());
            }
        }
        return taxes;
    }

    private void payUpkeep(long lastTax) {
        long total = getUpkeep();
        if (total <= 0) {
            return; // no upkeep, no message.
        }
        if (removeFromBank(total)) {
            SettlementsPlugin.getInstance().getLogging().info(getState().getTag() + " " + getName() + " (level " + getLevel() + ") was charged an upkeep of " + total + "!");
            MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.charged").getReplacedComponent("charged", String.valueOf(total)));
            if (!has(total - lastTax)) {
                MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.cant-pay").getReplacedComponent("amount", String.valueOf(total)));
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveSettlement(this);
                DatabaseManager.getInstance().log(this, null, "upkeep", "cash", total);
            });
        } else {
            if (getLevel() > 1 || getState() == SettlementState.KINGDOM) {
                if (getLevel() == 1 && getState() == SettlementState.KINGDOM) {
                    SettlementsPlugin.getInstance().getLogging().info(getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and has been downgraded!");
                    setState(SettlementState.VILLAGE);
                    setLevel(Config.Settlement.Leveling.VILLAGE_MAX_LEVEL);
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.downgraded").getReplacedComponent("state", getState().getTag(), "level", String.valueOf(getLevel())));
                } else {
                    SettlementsPlugin.getInstance().getLogging().info(getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and has had a level removed!");
                    setLevel(getLevel() - 1);
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.level-removed").getReplacedComponent("level", String.valueOf(getLevel())));
                }
                DatabaseManager.getInstance().saveSettlement(this);
            } else {
                setBank(0);
                if (Config.Settlement.Upkeep.ABANDON_TIME > 0) {
                    SettlementsPlugin.getInstance().getLogging().info(getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and was abandoned!");
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.abandoned").getComponent());
                    SettlementManager.getInstance().abandon(this);
                } else {
                    SettlementsPlugin.getInstance().getLogging().info(getState().getTag() + " " + getName() + " (level " + getLevel() + ") wasn't able to pay the upkeep of " + total + " and was deleted!");
                    MessageUtils.tellResidents(getOnlineResidents(), Message.get("upkeep.alert.deleted").getComponent());
                    SettlementManager.getInstance().delete(this);
                }
            }
        }
    }

    public long getUpkeep() {
        long amount = 0;
        for (OwnedLand land : LandManager.getInstance().getClaimedLand(this)) {
            amount += getClaimUpkeep(land.getWorld());
        }
        return amount;
    }

    public long getClaimUpkeep(String world) {
        return Math.max(
            0,
            SettlementManager.getInstance().getLevel(getState(), getLevel())
                .map(l -> l.getClaimUpkeep(world))
                .orElse(0L)
        );
    }

    public boolean isAbandoned() {
        return residents.isEmpty();
    }

    public long getAbandonTimeConfig() {
        return Config.Settlement.Upkeep.ABANDON_TIME;
    }

    public boolean canClaim(World world) {
        return !Config.Settlement.Claiming.DISABLED_WORLDS.contains(world.getName())
                && !Config.Claiming.DISABLED_WORLDS.contains(world.getName());
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        if (!ConfigManager.getInstance().getDataConfig().getConfig().contains(settlementState.getKey() + ".level" + level)) {
            ConfigManager.getInstance().getDataConfig().getConfig().set(settlementState.getKey() + ".level" + level, getName());
            ConfigManager.getInstance().getDataConfig().saveConfig();
            for (Player player : Bukkit.getOnlinePlayers()) {
                MessageUtils.tell(player, Message.get("first-to-reach-level").getReplacedComponent(
                        "name", this.getName(),
                        "state", settlementState.name().toLowerCase(),
                        "level", String.valueOf(level)
                ));
            }
        }
    }

    @Nullable
    public SettlementLevel getLevelInfo() {
        return SettlementManager.getInstance().getLevel(getState(), getLevel()).orElse(null);
    }

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
    public List<Resident> getResidents() {
        return residents.stream()
                .map(id -> ResidentManager.getInstance().getResident(id)).filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<Resident> getOnlineResidents() {
        return residents.stream()
                .map(Bukkit::getPlayer).filter(Objects::nonNull)
                .map(p -> ResidentManager.getInstance().getResident(p))
                .toList();
    }

    public long getOnlineCount() {
        return residents.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).count();
    }

    public boolean hasPermission(Resident resident, String perm) {
        return isMember(resident) && resident.hasPermission(perm.startsWith("settlement.") ? perm : "settlement." + perm);
    }

    public boolean isOfficer(Player player) {
        Resident res = ResidentManager.getInstance().getResident(player);
        return hasPermission(res, "officer");
    }

    @Override
    public boolean isMember(Resident resident) {
        if (resident == null) {
            return false;
        }
        return equals(resident.getSettlement());
    }

    public String getStateTag() {
        return getState().getTag();
    }

    public String getStateKey() {
        return getState().getKey();
    }

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

    public int getMaxClaimableChunks() {
        return SettlementManager.getInstance().getLevel(getState(), getLevel()).map(SettlementLevel::getChunks).orElse(0)
                + getBonusClaimAmount();
    }

    public int getMaxSize() {
        int maxSize = getLevelInfo() != null ? getLevelInfo().getSize() : -1;
        return maxSize > 0 ? maxSize : Config.Settlement.MAX_SIZE;
    }

    public boolean isValidName(String name) {
        return Pattern.matches(Config.Settlement.NAME_VALIDATION, name);
    }

    public boolean changeName(String name) {
        return SettlementManager.getInstance().changeName(this, name);
    }

    public int getBonusClaimAmount() {
        return getBonusClaims() + getPerksMap().entrySet().stream()
                .filter(e -> getPerk(e.getKey()) != null)
                .mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusClaims())
                .sum();
    }

    public int getMaxOutposts() {
        return SettlementManager.getInstance().getLevel(getState(), getLevel()).map(SettlementLevel::getOutposts).orElse(0)
                + getBonusOutposts();
    }

    public int getBonusOutposts() {
        return getPerksMap().entrySet().stream()
                .filter(e -> getPerk(e.getKey()) != null)
                .mapToInt(e -> e.getValue().getTimes() * getPerk(e.getKey()).getBonusOutposts())
                .sum();
    }

    @NotNull
    public Map<String, Long> getNextLevelRequirements() {
        return getNextLevel().map(SettlementLevel::getRequirements).orElse(Collections.emptyMap());
    }

    public boolean isMaxLevel() {
        return (getState().atLeast(SettlementState.KINGDOM) && getLevel() >= Config.Settlement.Leveling.KINGDOM_MAX_LEVEL) || getNextLevelRequirements().isEmpty();
    }

    public boolean checkLevelUp(Resident resident) {
        return SettlementManager.getInstance().checkLevelUp(this, resident);
    }

    public Collection<SettlementPerk> getPerks(int level) {
        return SettlementManager.getInstance().getPerks().stream()
            .filter(p -> p.getLevel() == level && p.getState() == settlementState)
            .toList();
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
        return Vaultable.super.getAmount(key);
    }

    @Override
    public void setAmount(String key, long amount) {
        if ("missions".equalsIgnoreCase(key)) {
            setFinishedMissions(amount);
        } else if ("missionpoints".equalsIgnoreCase(key)) {
            setMissionPoints(amount);
        } else {
            Vaultable.super.setAmount(key, amount);
        }
    }

    public long getFinishedMissions() {
        return finishedMissions;
    }

    public void setFinishedMissions(long finishedMissions) {
        this.finishedMissions = finishedMissions;
    }

    public Collection<SettlementMission> getMissions() {
        return SettlementManager.getInstance().getMissions();
    }

    public long getMissionRedistributePrice() {
        return Config.Settlement.Missions.REDISTRIBUTE_PRICE;
    }

    @Override
    public boolean setMode(PlotMode mode, boolean value) {
        if (ModeAble.super.setMode(mode, value)) {
            if (SettlementsPlugin.getInstance().isRunning() && mode == PlotMode.mobSpawning && !value) {
                for (OwnedLand land : LandManager.getInstance().getClaimedLand(this)) {
                    World world = Bukkit.getWorld(land.getWorldName());
                    if (world != null && world.isChunkLoaded(land.getX(), land.getZ())) {
                        for (Entity entity : land.getChunk().getEntities()) {
                            if (entity instanceof LivingEntity && Utils.isHostile(entity)) {
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

    public Collection<SettlementPerk> getAvailablePerks() {
        return SettlementManager.getInstance().getPerks();
    }

    public SettlementPerk getPerk(String name) {
        return SettlementManager.getInstance().getPerk(name);
    }

    public boolean isUnlockable(String unlockable) {
        return SettlementManager.getInstance().getUnlockables().contains(unlockable);
    }

    protected Set<String> getPermissions() {
        return RankManager.getInstance().getSettlementChiefRank().getInheritedPermissions();
    }

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
    public @NonNull LinkedHashMap<String, Object> serialize() {
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("id", getId().value());
        map1.put("name", getName());
        map1.put("description", getDescription());
        map1.put("rules", getRules());
        map1.put("discord", getDiscord());
        map1.put("created", getCreated());
        for (PlotMode mode : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            map1.put(mode.name(), getMode(mode));
        }
        map1.put("bank", getBank());
        map1.put("joinFee", getJoinFee());
        map1.put("tax", getTax());
        map1.put("auctionTax", getAuctionTax());
        map1.put("taxDebtAction", getTaxDebtAction().name());
        map1.put("bonusClaims", getBonusClaims());
        map1.put("buildState", getBuildState().name());
        map1.put("access", getAccess().name());
        map1.put("joinType", getJoinType().name());
        map1.put("joinRequests", getJoinRequests());
        if (getHome() != null)
            map1.put("homeLoc", getHome());
        else
            map1.put("homeLoc", new SimpleLocation(null, 0, 0, 0));
        map1.put("allies", getAllies());
        map1.put("allyRequests", getAllyRequests());
        map1.put("allowAllyClaim", isAllowAllyClaim());
        map1.put("banished", getBanished());
        map1.put("unbanished", getUnbanished());
        map1.put("uses", getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
        map1.put("vault", getVault());
        map1.put("perks", getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
        map1.put("permissions", getRanksPermissions());
        map1.put("titles", getRankTitles());
        map1.put("defaulttiles", getDefaultRankTitles());
        LinkedHashMap<String, Object> map = map1;
        map.put("chief", getChiefId());
        map.put("level", getLevel());
        map.put("settlementState", getState().name());
        map.put("finishedMissions", getFinishedMissions());
        map.put("missionPoints", getMissionPoints());
        map.put("residents", getResidentIds().stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("outposts", getOutpostHomes().values().stream().collect(Collectors.toMap(NamedLocation::getName, NamedLocation::serialize)));
        return map;
    }

    public void transfer(Resident newChief) {
        if (isMember(newChief)) {
            Resident oldChief = this.getChief();
            new SettlementChiefChangeEvent(this, this.getChief(), newChief).callEvent();
            oldChief.setSettlementRank(SettlementRank.getChief().next());
            newChief.setSettlementRank(SettlementRank.getChief());
            setChief(newChief.getUUID());
            DatabaseManager.getInstance().saveResident(oldChief);
            DatabaseManager.getInstance().saveResident(newChief);
            DatabaseManager.getInstance().saveSettlement(this);
        }
    }

    public void removeMember(Targetable target) {
        if (target instanceof Resident resident && isMember(resident)) {
            new SettlementLeaveEvent(this, resident).callEvent();
            residents.remove(resident.getUUID());
            SettlementManager.getInstance().leaveSettlement(resident);
        }
    }

    public void addMember(Targetable target) {
        if (target instanceof Resident resident && !isMember(resident)) {
            new SettlementJoinEvent(this, resident).callEvent();
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

    public String getTitle(Resident resident) {
        return resident.getSettlementTitle();
    }

    public boolean setTitle(Resident resident, String title) {
        return resident.setSettlementTitle(title);
    }

    public Collection<String> getTitles(SettlementRank rank) {
        Collection<String> titles = rankTitles.get(rank.name().toLowerCase());
        if (titles.isEmpty()) {
            return rank.getTitles(this);
        }
        return titles;
    }

    public boolean addTitle(SettlementRank rank, String title) {
        if (rankTitles.get(rank.name().toLowerCase()).isEmpty()) {
            if (getTitle(rank, title) != null) {
                return false;
            } else {
                rankTitles.putAll(rank.name().toLowerCase(), rank.getTitles(this));
            }
        }
        if (rankTitles.put(rank.name().toLowerCase(), title)) {
            save();
            return true;
        }
        return false;
    }

    public boolean removeTitle(SettlementRank rank, String title) {
        if (rankTitles.get(rank.name().toLowerCase()).isEmpty()) {
            if (getTitle(rank, title) != null) {
                rankTitles.putAll(rank.name().toLowerCase(), rank.getTitles(this));
            } else {
                return false;
            }
        }
        String titleCase = getTitle(rank, title);
        if (rankTitles.remove(rank.name().toLowerCase(), titleCase)) {
            if (getDefaultTitle(rank).equals(title)) {
                defaultRankTitles.remove(rank.name().toLowerCase());
            }
            save();
            return true;
        }
        return false;
    }

    public int getTitlesPerRank() {
        return Config.Settlement.TITLES_PER_RANK;
    }

    public SettlementRank getHighestRank() {
        return SettlementRank.getChief();
    }

    public SettlementRank getDefaultRank() {
        return SettlementRank.getDefault();
    }

    public boolean useGuestRank() {
        return true;
    }

    public SettlementRank getRank(Resident resident) {
        return isMember(resident) ? resident.getSettlementRank() : null;
    }

    public Collection<SettlementRank> getRanks() {
        return RankManager.getInstance().getSettlementRanks();
    }

    public Collection<OwnedLand> getLand() {
        return LandManager.getInstance().getClaimedLand(this);
    }

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

    public long getMaxTax() {
        return Config.Settlement.MAX_TAX;
    }

    public double getMaxAuctionTax() {
        return Config.Settlement.MAX_AUCTION_TAX;
    }

    @SuppressWarnings("unchecked")
    public static Settlement deserialize(Map<String, Object> map) {
        SettlementsPlugin.logDebug("Creating settlement from " + map);
        Settlement settlement = Settlement.deserialize(new Settlement(), map);
        if (settlement == null) {
            return null;
        }
        if (map == null || map.get("chief") == null || map.get("level") == null || map.get("settlementState") == null) {
            SettlementsPlugin.getInstance().getLogging().warn("Could not deserialize Settlement! (" + map + ")");
            return null;
        }
        settlement.setChief(UUID.fromString((String) map.get("chief")));
        settlement.setState(SettlementState.valueOf((String) map.get("settlementState")));
        settlement.setLevel((Integer) map.get("level"));
        settlement.setFinishedMissions(Utils.getLong(map.get("finishedMissions")));
        settlement.setMissionPoints(Utils.getLong(map.get("missionPoints")));
        ((Collection<String>) map.get("residents")).stream().map(UUID::fromString).forEach(u -> settlement.getResidentIds().add(u));
        ((Map<String, Map<String, Object>>) map.get("outposts")).values().stream()
                .map(NamedLocation::deserialize)
                .filter(Objects::nonNull)
                .forEach(settlement::setOutpostHome);
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

    public boolean canAdminLocks(Resident resident) {
        return hasPermission(resident, "lock.admin");
    }

    public boolean canAdminPlots(Resident resident) {
        return hasPermission(resident, "plot.admin");
    }

    public boolean canAdminPortals(Resident resident) {
        return hasPermission(resident, "portal.admin");
    }

    public IdObject getId() {
        return id;
    }

    public void setId(IdObject id) {
        if (id == null) {
            id = IdObject.get(null);
        }
        this.id = id;
    }

    public UUID getUUID() {
        return UUID.nameUUIDFromBytes(getIdKey().getBytes());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public void setDiscord(String discordLink) {
        this.discordLink = discordLink;
    }

    public String getDiscord() {
        return discordLink;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public Map<String, PerkData> getPerksMap() {
        return perks;
    }

    public boolean hasUnlocked(String unlockable) {
        unlockable = unlockable.toLowerCase(Locale.ROOT);
        if (unlockable.startsWith("settlements.")) {
            unlockable = unlockable.substring("settlements.".length());
        }
        if (unlockable.contains(getKey() + ".")) {
            unlockable = unlockable.replace(getKey() + ".", "");
        }
        if (!isUnlockable(unlockable)) {
            return true;
        }
        for (Perk perk : getPerks()) {
            if (perk.getUnlockables().contains(unlockable.toLowerCase(Locale.ROOT))
                    && hasUnlocked(perk)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasUnlocked(Perk perk) {
        return perk != null && getPerksMap().containsKey(perk.name().toLowerCase(Locale.ROOT));
    }

    public Collection<SettlementPerk> getPerks() {
        return getPerksMap().keySet().stream()
                .map(this::getPerk)
                .filter(Objects::nonNull)
                .toList();
    }

    public PerkData getPerkData(Perk perk) {
        return getPerksMap().get(perk.getKey().toLowerCase());
    }

    public boolean canUnlock(Perk perk) {
        return !hasUnlocked(perk) && has(perk.getUnlockCost());
    }

    public boolean canRepeat(Perk perk) {
        return hasUnlocked(perk) && has(perk.getRepeatCost()) && checkPerkCount(perk);
    }

    public boolean checkPerkCount(Perk perk) {
        if (perk.getRepeat() > 0) {
            PerkData p = getPerkData(perk);
            if (p != null) {
                return p.getTimes() <= perk.getRepeat();
            }
            return true;
        }
        return perk.getRepeat() < 0;
    }

    public boolean checkPerkCooldown(Perk perk) {
        if (perk.getCooldown() > 0) {
            PerkData p = getPerkData(perk);
            if (p != null) {
                return p.getLastUsed() + perk.getCooldown() * 1000 < System.currentTimeMillis();
            }
        }
        return true;
    }

    public boolean unlockPerk(Resident resident, Perk perk) {
        if (!hasUnlocked(perk)) {
            getPerksMap().put(perk.name().toLowerCase(), new PerkData());
            apply(resident.getPlayer(), perk);
            new PerkUnlockedEvent(this, resident, perk).callEvent();
            return true;
        }
        return false;
    }

    public void apply(Player player, Perk perk) {
        if (perk.hasEffects()) {
            applyEffects(player, perk);
        }
        runCommands(perk);
    }

    public boolean repeatPerk(Player player, Perk perk) {
        PerkData p = getPerkData(perk);
        if (p != null) {
            p.addUse();
            apply(player, perk);
            return true;
        }
        return false;
    }

    public boolean togglePerk(Player player, Perk perk) {
        if (perk.hasEffects() || perk.hasActions()) {
            PerkData perkData = getPerkData(perk);
            if (perkData != null) {
                perkData.toggle();
                if (perkData.isActive()) {
                    applyEffects(player, perk);
                } else {
                    removeEffects(player, perk);
                }
                save();
                return true;
            }
        }
        return false;
    }

    public void applyEffects(Player player, Perk perk) {
        SettlementsPlugin.getInstance().getLandPerks().updateEffects(false);
        perk.toggleActions(player, this, true);
    }

    public void removeEffects(Player player, Perk perk) {
        SettlementsPlugin.getInstance().getLandPerks().updateEffects(false);
        perk.toggleActions(player, this, false);
    }

    public Map<String, Map<String, Boolean>> getRanksPermissions() {
        return ranksPermissions;
    }

    public Map<String, Boolean> getPermissions(SettlementRank rank) {
        return ranksPermissions.getOrDefault(rank.name().toLowerCase(), Collections.emptyMap());
    }

    public Map<String, Optional<Boolean>> getEffectivePermissions(SettlementRank rank) {
        Map<String, Optional<Boolean>> map = new LinkedHashMap<>();
        for (String perm : getPermissions()) {
            map.put(perm, getEffectivePermissionValue(rank, perm));
        }
        return map;
    }

    public Optional<Boolean> getPermissionValue(SettlementRank rank, String permission) {
        if (permission.contains(".group.")) {
            return RankManager.getInstance().getFromGroup(permission, p -> getPermissionValue(rank, p));
        }
        Map<String, Boolean> map = ranksPermissions.get(rank.name().toLowerCase());
        return Optional.ofNullable(map != null ? map.get(permission.toLowerCase()) : null);
    }

    public Optional<Boolean> getInheritedPermissionValue(SettlementRank rank, String permission) {
        if (permission.contains(".group.")) {
            return RankManager.getInstance().getFromGroup(permission, p -> getInheritedPermissionValue(rank, p));
        }

        Optional<Boolean> value = getPermissionValue(rank, permission);
        if (value.isPresent()) {
            return value;
        }
        if (rank.getInherits() != null) {
            return getInheritedPermissionValue((SettlementRank) rank.getInheritedRank(), permission);
        }
        return Optional.empty();
    }

    public Optional<Boolean> getEffectivePermissionValue(SettlementRank rank, String permission) {
        if (permission.contains(".group.")) {
            return RankManager.getInstance().getFromGroup(permission, p -> getEffectivePermissionValue(rank, p));
        }

        if (rank.getParent() != null) {
            Optional<Boolean> inheritValue = getInheritedPermissionValue(rank, permission);
            if (inheritValue.isPresent()) {
                return inheritValue;
            }
        }
        return rank.getInheritedPermissionValue(permission);
    }

    public boolean setPermission(SettlementRank rank, String permission, boolean value) {
        if (permission.contains(".group.")) {
            Optional<Boolean> changed = RankManager.getInstance().applyToGroup(permission, p -> setPermission(rank, p, value));
            if (changed.isPresent()) {
                return changed.get();
            }
        }

        if (getEffectivePermissionValue(rank, permission).orElse(false) == value) {
            return false;
        }

        if (rank.getPermissionValue(permission) == value) {
            return setDefaultPermission(rank, permission);
        }

        Boolean prev = ranksPermissions.computeIfAbsent(rank.name().toLowerCase(), k -> new LinkedHashMap<>()).put(permission.toLowerCase(), value);
        return prev == null || prev != value;
    }

    public boolean setDefaultPermission(SettlementRank rank, String permission) {
        if (permission.contains(".group.")) {
            Optional<Boolean> changed = RankManager.getInstance().applyToGroup(permission, p -> setDefaultPermission(rank, p));
            if (changed.isPresent()) {
                return changed.get();
            }
        }
        return ranksPermissions.getOrDefault(rank.name().toLowerCase(), new LinkedHashMap<>()).remove(permission.toLowerCase()) != null;
    }

    public String getIdKey() {
        return getKey() + ":" + getId();
    }

    public BuildState getUseState(UseFlag useFlag, Use u) {
        return use.getOrDefault(u, useFlag != null
                ? useFlag.getDefault(getDefaultBuildState()) : getDefaultBuildState());
    }

    public Map<Use, BuildState> getUseStates() {
        return use;
    }

    @Override
    public Map<String, Long> getVault() {
        return vault;
    }

    public long getBank() {
        return bank;
    }

    public void setBank(long bank) {
        this.bank = bank;
    }

    public boolean setUseState(Use u, BuildState state) {
        if (state == null) {
            return use.remove(u) != null;
        } else {
            return use.put(u, state) != null;
        }
    }

    public boolean unsetUseState(UseFlag u) {
        boolean r = false;
        for (Use useType : u.getUseTypes()) {
            r |= use.remove(useType) != null;
        }
        return r;
    }

    public boolean hasUseState(Use u) {
        return use.containsKey(u);
    }

    public FlagAble getParent() {
        return new FlagAble() {
            @Override
            public void setBuildState(BuildState buildState) {

            }

            @Override
            public BuildState getBuildState() {
                return getDefaultBuildState();
            }

            @Override
            public BuildState getUseState(UseFlag useFlag, Use u) {
                return useFlag != null ? useFlag.getDefault(getDefaultBuildState()) : getDefaultBuildState();
            }

            @Override
            public Map<Use, BuildState> getUseStates() {
                return Collections.emptyMap();
            }

            @Override
            public boolean setUseState(Use u, BuildState state) {
                return false;
            }

            @Override
            public boolean unsetUseState(UseFlag u) {
                return false;
            }

            @Override
            public boolean hasUseState(Use u) {
                return false;
            }

            @Override
            public BuildState getDefaultBuildState() {
                return Settlement.this.getDefaultBuildState();
            }

            @Override
            public BuildState.AppliesTo getBuildStateType() {
                return Settlement.this.getBuildStateType();
            }

            @Override
            public void save() {

            }

            @Override
            public FlagAble getParent() {
                return null;
            }

            @Override
            public String getStateTag() {
                return Settlement.this.getStateTag();
            }

            @Override
            public String getBuildTag() {
                return Settlement.this.getTag();
            }
        };
    }

    public void runCommands(Perk perk) {
        Utils.runAsync(() -> {
            List<String> commands = new ArrayList<>();
            for (String command : perk.getCommands()) {
                for (Resident resident : getResidents()) {
                    commands.add(StringUtils.parsePlaceholders(command,
                            "player", resident.getName(),
                            "playerid", resident.getUUID().toString(),
                            "settlement", getName(),
                            "state", getStateTag(),
                            "level", Settlement.this instanceof Settlement ? String.valueOf(Settlement.this.getLevel()) : "-1",
                            "perk", perk.getTag()
                    ));
                }
            }
            Utils.runSync(() -> {
                for (String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            });
        });
    }

    @Override
    public int getBonusClaims() {
        return bonusClaims;
    }

    @Override
    public void setBonusClaims(int bonusClaims) {
        this.bonusClaims = bonusClaims;
    }

    public boolean isBanished(Targetable target) {
        if (target instanceof Resident resident) {
            return banished.contains("player", IdObject.get(resident.getUUID()))
                    || (resident.hasSettlement() && isBanished(resident.getSettlement()));
        } else if (target instanceof Settlement owner) {
            return banished.contains(owner.getKey(), owner.getId());
        }
        return false;
    }

    public boolean addBanished(Resident sender, Targetable target, String reason) {
        if (isBanished(target)) {
            return false;
        }
        Banished entry;
        if (target instanceof Resident res) {
            entry = new Banished(
                    "player",
                    IdObject.get(res.getUUID()),
                    System.currentTimeMillis(),
                    sender != null ? sender.getUUID() : Utils.ZERO_UUID,
                    reason
            );
        } else if (target instanceof Settlement owner) {
            entry = new Banished(
                    owner.getKey(),
                    target.getId(),
                    System.currentTimeMillis(),
                    sender != null ? sender.getUUID() : Utils.ZERO_UUID,
                    reason
            );
        } else {
            return false;
        }
        return banished.put(entry.type(), entry.banishedId(), entry) == null;
    }

    public boolean removeBanished(Resident sender, Targetable target) {
        if (target instanceof Resident res) {
            Banished entry = banished.remove("player", IdObject.get(res.getUUID()));
            if (entry != null) {
                unbanished.add(entry.toUnbanish(sender));
                return true;
            }
            return false;
        } else if (target instanceof Settlement owner) {
            Banished entry = banished.remove(owner.getDbName(), owner.getId());
            if (entry != null) {
                unbanished.add(entry.toUnbanish(sender));
                return true;
            }
        }
        return false;
    }

    public Ally addAlly(Settlement landOwner) {
        Ally ally = new Ally(landOwner.getKey(), landOwner.getId(), System.currentTimeMillis(), isAllowAllyClaim());
        allies.put(landOwner.getKey(), landOwner.getId(), ally);
        return ally;
    }

    public Ally removeAlly(Settlement landOwner) {
        return allies.remove(landOwner.getKey(), landOwner.getId());
    }

    public boolean isAlly(Settlement landOwner) {
        return allies.contains(landOwner.getKey(), landOwner.getId());
    }

    public boolean isAlly(Resident resident) {
        return isMember(resident)
                || (resident.hasSettlement() && isAlly(resident.getSettlement()));
    }

    public boolean hasAllies() {
        return !getAllies().isEmpty();
    }

    public boolean hasSettlementAllies() {
        return getAllies().stream().anyMatch(a -> a.getType().equals("settlement"));
    }

    public boolean hasAllyRequest(Settlement landOwner) {
        return allyRequests.contains(landOwner.getKey(), landOwner.getId());
    }

    public AllyRequest addAllyRequest(AllyRequest request) {
        return allyRequests.put(request.getSourceType(), request.getSourceId(), request);
    }

    public AllyRequest removeAllyRequest(Settlement landOwner) {
        return allyRequests.remove(landOwner.getKey(), landOwner.getId());
    }

    public AllyRequest removeAllyRequest(AllyRequest request) {
        return allyRequests.remove(request.getSourceType(), request.getSourceId());
    }

    public Collection<AllyRequest> getAllyRequests() {
        return allyRequests.values();
    }

    public boolean hasJoinRequest(Targetable targetable) {
        return joinRequests.containsKey(targetable.getId());
    }

    public JoinRequest<?> addJoinRequest(JoinRequest<?> request) {
        return joinRequests.put(request.getSourceId(), request);
    }

    public JoinRequest<?> removeJoinRequest(Targetable targetable) {
        return joinRequests.remove(targetable.getId());
    }

    public JoinRequest<?> removeJoinRequest(JoinRequest<?> request) {
        return joinRequests.remove(request.getSourceId());
    }

    public Collection<JoinRequest<?>> getJoinRequests() {
        return joinRequests.values();
    }

    @Override
    public Map<PlotMode, Boolean> getModes() {
        return modes;
    }

    public SimpleLocation getHome() {
        return home;
    }

    public boolean hasHome() {
        return this.getHome() != null;
    }

    public Collection<Banished> getBanished() {
        return banished.values();
    }

    public List<Unbanished> getUnbanished() {
        return unbanished;
    }

    public Ally getAlly(Settlement landOwner) {
        return allies.get(landOwner.getKey(), landOwner.getId());
    }

    public Collection<Ally> getAllies() {
        return allies.values();
    }

    public boolean isAllowAllyClaim() {
        return allowAllyClaim;
    }

    public void setAllowAllyClaim(boolean allowAllyClaim) {
        this.allowAllyClaim = allowAllyClaim;
    }

    public void setHome(SimpleLocation home) {
        this.home = home;
    }

    public void setHome(Location location) {
        setHome(new SimpleLocation(location));
    }

    public long getJoinFee() {
        return joinFee;
    }

    public void setJoinFee(long joinFee) {
        this.joinFee = joinFee;
    }

    public long getTax() {
        if (getMaxTax() > -1 && tax > getMaxTax() ) {
            return getMaxTax();
        }
        return tax;
    }

    public void setTax(long tax) {
        this.tax = tax;
    }

    public double getAuctionTax() {
        if (getMaxAuctionTax() > -1 && auctionTax > getMaxAuctionTax() ) {
            return getMaxAuctionTax();
        }
        return auctionTax;
    }

    public void setAuctionTax(long auctionTax) {
        setAuctionTax(auctionTax / 100d);
    }

    public void setAuctionTax(double auctionTax) {
        this.auctionTax = auctionTax;
    }

    public TaxDebtAction getTaxDebtAction() {
        return taxDebtAction;
    }

    public void setTaxDebtAction(TaxDebtAction taxDebtAction) {
        this.taxDebtAction = taxDebtAction;
    }

    public void setBuildState(BuildState buildState) {
        this.buildState = buildState;
    }

    public BuildState getBuildState() {
        return this.buildState;
    }

    public LandAccess getAccess() {
        return access;
    }

    public void setAccess(LandAccess access) {
        this.access = access;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public boolean isChief(Targetable target) {
        if (target instanceof Resident) {
            return isChief(((Resident) target).getUUID());
        }
        return false;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public void setDefaultTitle(SettlementRank rank, String title) {
        String t = getTitle(rank, title);
        defaultRankTitles.put(rank.name().toLowerCase(), title);
        if (t == null) {
            addTitle(rank, title);
        } else {
            save();
        }
    }

    public String getDefaultTitle(SettlementRank rank) {
        return defaultRankTitles.getOrDefault(rank.name(), getTitles(rank).iterator().next());
    }

    public String getTitle(SettlementRank rank, String title) {
        for (String rankTitle : getTitles(rank)) {
            if (rankTitle.equalsIgnoreCase(title)) {
                return rankTitle;
            }
        }
        return null;
    }

    public Map<String, Collection<String>> getRankTitles() {
        return rankTitles.asMap();
    }

    public Map<String, String> getDefaultRankTitles() {
        return defaultRankTitles;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass().equals(getClass()) && ((Settlement) o).getId().equals(getId());
    }

    @Override
    public void logDeposit(Resident resident, Map<String, Long> deposited) {
        Utils.runAsync(() -> DatabaseManager.getInstance().logDeposit(this, resident, deposited));
    }

    public boolean canLevelUp() {
        if (isMaxLevel()) {
            return false;
        }
        return has(getNextLevelRequirements());
    }

    public double getNextLevelProgress() {
        Map<String, Long> requirements = getNextLevelRequirements();
        if (requirements.isEmpty()) {
            return 1;
        }

        double totalProgress = 0;

        for (Map.Entry<String, Long> requirement : requirements.entrySet()) {
            totalProgress += Math.min(1, (double) getAmount(requirement.getKey()) / requirement.getValue());
        }

        return totalProgress / requirements.size();
    }

    public enum Placeholder {
        name((c, v) -> v.getName()),
        description((c, v) -> v.getDescription()),
        state((c, v) -> v.getStateTag()),
        level((c, v) -> String.valueOf(v.getLevel())),
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
        recruitment((c, v) -> v.getJoinType().getTag()),
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
                                    "title", ""
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

    public enum Sort implements Sortable.Sort<Settlement> {
        LEVEL(Settlement::compareTo),
        SIZE((s1, s2) -> Integer.compare(s2.getResidentIds().size(), s1.getResidentIds().size())),
        NAME((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName())),
        PRIVACY(Comparator.comparing(Settlement::getAccess)),
        RECRUITMENT(Comparator.comparing(Settlement::getJoinType)),
        AGE((s1, s2) -> Long.compare(s2.getCreated(), s1.getCreated()));

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
