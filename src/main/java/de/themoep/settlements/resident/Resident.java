package de.themoep.settlements.resident;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.land.LandManager;

import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.TaxDebtAction;
import de.themoep.settlements.missions.Mission;
import de.themoep.settlements.missions.MissionData;
import de.themoep.settlements.rank.PermissionGroup;
import de.themoep.settlements.rank.RankManager;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.SettlementMission;
import de.themoep.settlements.utils.ColorUtils;
import de.themoep.settlements.utils.ComponentUtils;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.firedev.chatchannels.api.ChatChannel;
import uk.firedev.daisylib.external.vault.VaultWrapper;

import java.sql.Date;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Resident implements ConfigurationSerializable, DatabaseSerializable, Targetable, Sortable<Resident> {
    private IdObject id = IdObject.get(null);
    private final UUID uuid;
    private String name;
    private SettlementRank rank = SettlementRank.getDefault();
    private String settlementTitle = null;
    private Settlement settlement = null;
    private final Map<UUID, Friend> friends = new ConcurrentHashMap<>();
    private final Map<String, FriendRequest> friendRequests = new ConcurrentHashMap<>();
    private boolean swear = true;
    private boolean socialSpy = false;
    private long missionPoints = 0;
    private final Map<String, MissionData> missionData = new LinkedHashMap<>();
    private long bonusClaims = 0;
    private long lastLogin = 0;
    private long settlementJoin;
    private boolean taxExempted = false;
    private TaxDebtAction taxDebtAction = TaxDebtAction.KICK;
    private long taxPaid = 0;
    private long donated = 0;

    // Temporary, per session settings:
    private boolean resourcePack = false;
    private boolean showSidebar = true;
    private boolean hasMessagesEnabled = true;
    private boolean notifyEnabled = true;
    private final Cache<IdObject, Boolean> closeBuildingNotificationToggles = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(Config.Claiming.Close.Building.TOGGLE_TIME))
            .build();

    public Resident(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    /**
     * Tests if a resident has a certain permission based on his rank or Bukkit permissions
     * @param permission    Permission to check
     * @return              The value of the permission or false
     */
    public boolean hasPermission(String permission) {
        if (permission.contains(".group.")) {
            PermissionGroup group = RankManager.getInstance().getPermissionGroup(permission);
            if (group != null) {
                for (String perm : group.getPermissions()) {
                    if (!hasPermission(perm)) {
                        return false;
                    }
                }
                return true;
            }
        }

        if (hasRankPermission(permission)) {
            Player player = getPlayer();
            if (player != null) {
                if (!permission.startsWith("settlements.")) {
                    permission = "settlements.command." + permission;
                }
                return player.hasPermission(permission);
            }
        }
        return false;
    }

    public boolean hasRankPermission(String permission) {
        if (permission.contains(".group.")) {
            Optional<Boolean> r = RankManager.getInstance().applyToGroup(permission, this::hasRankPermission);
            if (r.isPresent()) {
                return r.get();
            }
        }

        String rankPerm = permission;
        if (permission.startsWith("settlements.command.")) {
            rankPerm = permission.substring("settlements.command.".length());
        }

        if (!hasSettlement() && SettlementRank.getChief().getInheritedPermissionValue(rankPerm).isEmpty()) {
            return true;
        }

        Optional<Boolean> settlementValue = getSettlement().getEffectivePermissionValue(getSettlementRank(), rankPerm);
        if (settlementValue.isPresent()) {
            return settlementValue.get();
        }
        if (hasSettlement()) {
            return getSettlement().getEffectivePermissionValue(getSettlement().getHighestRank(), rankPerm).isEmpty();
        }
        return SettlementRank.getChief().getInheritedPermissionValue(rankPerm).isEmpty();
    }

    public boolean hasSettlement() {
        return settlement != null;
    }

    public Collection<Friend> getFriends() {
        return friends.values();
    }

    public boolean isFriend(Resident resident) {
        return friends.containsKey(resident.getUUID());
    }

    public boolean isFriend(UUID playerId) {
        return friends.containsKey(playerId);
    }

    public boolean addFriend(Friend friend) {
        return friends.put(friend.getPlayerId(), friend) == null;
    }

    public boolean addFriend(UUID id, String name) {
        return friends.put(id, new Friend(id, name)) == null;
    }

    public boolean removeFriend(UUID id) {
        return friends.remove(id) != null;
    }

    public boolean addFriendRequest(FriendRequest friendRequest) {
        return friendRequests.put(friendRequest.getSenderName().toLowerCase(Locale.ROOT), friendRequest) == null;
    }

    public FriendRequest removeFriendRequest(String playerName) {
        return friendRequests.remove(playerName.toLowerCase(Locale.ROOT));
    }

    public FriendRequest getFriendRequest(String playerName) {
        return friendRequests.get(playerName.toLowerCase(Locale.ROOT));
    }

    public Collection<FriendRequest> getFriendRequests() {
        return friendRequests.values();
    }

    public MissionData getMissionData(String missionId) {
        return missionData.get(missionId.toLowerCase(Locale.ROOT));
    }

    public Collection<MissionData> getMissionData() {
        return missionData.values();
    }

    public int getExtraMissions() {
        return 0;
    }

    public Map<String, Map<String, Long>> getMissionDataMap() {
        return getMissionData().stream().collect(Collectors.toMap(md -> md.getMission().getDbId(), MissionData::getProgress));
    }

    public void addMissionData(MissionData md) {
        missionData.put(md.getMission().getId().toLowerCase(Locale.ROOT), md);
    }

    public long getMissionPoints() {
        return missionPoints;
    }

    public void addMissionPoints(long missionPoints) {
        this.missionPoints += missionPoints;
    }

    public void removeMissionPoints(long missionPoints) {
        this.missionPoints -= missionPoints;
    }

    public void setMissionPoints(long missionPoints) {
        this.missionPoints = missionPoints;
    }

    public Settlement getMissionProvider() {
        return getSettlement();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSettlementName() {
        return settlement != null ? settlement.getName() : "";
    }

    public void setSettlement(Settlement settlement) {
        if (getSettlement() != null) {
            getSettlement().getResidentIds().remove(getUUID());
        }
        this.settlement = settlement;
        if (settlement != null) {
            settlement.getResidentIds().add(getUUID());
            setSettlementJoin(System.currentTimeMillis());
        } else {
            setSettlementRank(SettlementRank.getDefault());
            setSettlementJoin(-1);
            setSettlementTitle(null);
        }
    }

    public Settlement getSettlement() {
        return settlement;
    }

    public boolean leaveSettlement() {
        if (hasSettlement()) {
            getSettlement().removeMember(this);
            return true;
        }
        return false;
    }

    public void setSettlementRank(SettlementRank rank) {
        if (this.rank == rank) {
            return;
        }
        this.rank = rank;
    }

    public SettlementRank getSettlementRank() {
        return rank;
    }

    public String getSettlementTitle() {
        return hasSettlement()
                ? (settlementTitle != null
                        ? (getSettlement().getTitle(getSettlementRank(), settlementTitle) != null
                                ? settlementTitle : getSettlementRank().getTag(getSettlement()))
                        : getSettlementRank().getTag(getSettlement()))
                : "";
    }

    public String getDisplayTitle() {
        if (hasSettlement()) {
            return Message.get("chat.format.title.settlement").getReplacedText(
                    "settlementTitle", getSettlementTitle()
            );
        }
        return Message.get("chat.format.title.none").getText();
    }

    public boolean setSettlementTitle(String title) {
        if (!hasSettlement()) {
            this.settlementTitle = null;
        } else if (title == null) {
            this.settlementTitle = getSettlementRank().getTag(getSettlement());
        } else if (getSettlement().getTitle(getSettlementRank(), title) != null) {
            this.settlementTitle = getSettlement().getTitle(getSettlementRank(), title);
        } else {
            return false;
        }
        return true;
    }

    public long getBonusClaims() {
        return bonusClaims;
    }

    public void setBonusClaims(long bonusClaims) {
        this.bonusClaims = bonusClaims;
    }

    public long getTax() {
        return getSettlementTax() + getPlotTax();
    }

    public long getSettlementTax() {
        if (hasSettlement() && !hasPermission("settlement.tax.exempt")) {
            return getSettlement().getTax();
        }
        return 0;
    }

    public long getPlotTax() {
        long tax = 0;
        for (OwnedLand land : LandManager.getInstance().getOwnedPlot(getUUID())) {
            if (!hasSettlement()
                    || !land.getSettlement().isMember(this)
                    || (!hasPermission(land.getSettlement().getKey() + ".tax.exempt")
                    && !land.getSettlement().isChief(getSettlement()))) {
                tax += land.getTax();
            }
        }
        return tax;
    }

    @Override
    public List<Resident> getOnlineResidents() {
        return getPlayer() == null ? new ArrayList<>() : Collections.singletonList(this);
    }

    @Override
    public boolean isMember(Resident resident) {
        return equals(resident);
    }

    public long getSettlementJoin() {
        return getSettlement() != null ? settlementJoin : -1;
    }

    public void setSettlementJoin(long settlementJoin) {
        this.settlementJoin = settlementJoin;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void updateLastLogin() {
        lastLogin = System.currentTimeMillis();
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * Temporary, per session setting
     */
    public void setResourcePack(boolean resourcePack) {
        this.resourcePack = resourcePack;
    }

    /**
     * Temporary, per session setting
     */
    public boolean hasResourcePack() {
        return resourcePack;
    }

    /**
     * Temporary, per session setting
     */
    public boolean showSidebar() {
        return showSidebar;
    }

    /**
     * Temporary, per session setting
     */
    public void showSidebar(boolean showSidebar) {
        this.showSidebar = showSidebar;
    }

    public String[] getReplacements(String... initial) {
        List<String> replacements = new ArrayList<>();
        Collections.addAll(replacements, initial);

        for (Placeholder replacement : Placeholder.values()) {
            replacements.add(replacement.name());
            replacements.add(replacement.getValue(this));
        }

        return replacements.toArray(new String[0]);
    }

    public String getReplacement(String placeholder) {
        try {
            return Placeholder.valueOf(placeholder).getValue(this);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public TaxDebtAction getTaxDebtAction() {
        return taxDebtAction;
    }

    public void setTaxDebtAction(TaxDebtAction taxDebtAction) {
        this.taxDebtAction = taxDebtAction;
    }

    public long getTaxPaid() {
        return taxPaid;
    }

    public void addTaxPaid(long amount) {
        taxPaid += amount;
    }

    public void setTaxPaid(int amount) {
        this.taxPaid = amount;
    }

    public long getDonated() {
        return donated;
    }

    public void addDonated(long amount) {
        donated += amount;
    }

    public void setDonated(int amount) {
        this.donated = amount;
    }

    public boolean isTaxExempted() {
        return taxExempted;
    }

    public void setTaxExempted(boolean taxExempted) {
        this.taxExempted = taxExempted;
    }

    public void save() {
        DatabaseManager.getInstance().saveResident(this);
    }

    public double getBalance() {
        return SettlementsPlugin.getInstance().getEconomy().getBalance(Bukkit.getOfflinePlayer(getUUID()));
    }

    public boolean hasUnlocked(String unlock) {
        return hasSettlement() && getSettlement().hasUnlocked(unlock);
    }

    @Override
    public String[] getReplacements(ConfigMessages configMessages, String... initial) {
        return getReplacements(initial);
    }

    @Override
    public Sortable.Sort<Resident>[] getSorts() {
        return Sort.values();
    }

    @Override
    public ItemStack getIcon() {
        return ItemUtils.getHead(uuid, name);
    }

    public boolean hasMission(SettlementMission mission) {
        return getMissionData(mission.getId()) != null;
    }

    public int distributeNewMissions() {
        return getMissionProvider().distributeNewMissions();
    }

    public enum Sort implements Sortable.Sort<Resident> {
        RANK(Comparator.comparing(Resident::getSettlementRank)),
        LAST_LOGIN((r1, r2) -> Long.compare(r2.getLastLogin(), r1.getLastLogin())),
        ;

        private final Comparator<Resident> comparator;

        Sort(Comparator<Resident> comparator) {
            this.comparator = comparator;
        }

        @Override
        public Sortable.Sort<Resident>[] getValues() {
            return Sort.values();
        }

        @Override
        public Comparator<Resident> getComparator() {
            return comparator;
        }
    }

    public enum Placeholder {
        name(Resident::getName),
        title(r -> r.hasSettlement() ? r.getDisplayTitle() : "none"),
        settlement(r -> r.hasSettlement() ? r.getSettlement().getName() : "none"),
        settlementstate(r -> r.hasSettlement() ? r.getSettlement().getStateTag() : ""),
        rank(r -> r.hasSettlement() ? r.getSettlementRank().getTag(r.getSettlement()) : "none"),
        settlementrank(r -> r.hasSettlement() ? r.getSettlementRank().getTag(r.getSettlement()) : "none"),
        settlementtitle(r -> r.hasSettlement() ? r.getSettlementTitle() : "none"),
        balance(r -> Utils.CURRENCY_FORMAT.format(r.getBalance())),
        tax(r -> Utils.CURRENCY_FORMAT.format(r.getTax())),
        bonus(r -> Utils.CURRENCY_FORMAT.format(r.getBonusClaims())),
        missionpoints(r -> String.valueOf(r.getMissionPoints())),
        joindate(r -> Utils.DATE_FORMAT.format(new Date(Bukkit.getOfflinePlayer(r.getUUID()).getFirstPlayed()))),
        lastlogin(r -> Utils.DATE_FORMAT.format(new Date(r.getLastLogin()))),
        lastloginduration(r -> {
             Player player = r.getPlayer();
             if (player != null && player.isOnline()) {
                 return Message.get("names.online").getText();
             }
             return UtilTime.format(System.currentTimeMillis() - r.getLastLogin(), false) + " " + Message.get("names.ago").getText();
        }),
        plots(r -> String.valueOf(LandManager.getInstance().getOwnedPlotCount(r.getUUID()))),
        primarygroup(r -> {
            Player player = r.getPlayer();
            if (player != null) {
                return SettlementsPlugin.getInstance().getChat().getPrimaryGroup(player);
            }
            return "Unknown";
        }),
        vaultprefix(r -> {
            Player player = r.getPlayer();
            if (player != null) {
                String group = SettlementsPlugin.getInstance().getChat().getPrimaryGroup(player);
                return VaultWrapper.get().getChat().getGroupPrefix((String) null, group);
            }
            return "Unknown";
        }),
        vaultsuffix(r -> {
            Player player = r.getPlayer();
            if (player != null) {
                String group = SettlementsPlugin.getInstance().getChat().getPrimaryGroup(player);
                return VaultWrapper.get().getChat().getGroupSuffix((String) null, group);
            }
            return "Unknown";
        }),
        ;

        private final Function<Resident, String> valueProvider;

        Placeholder(Function<Resident, String> valueProvider) {
            this.valueProvider = valueProvider;
        }

        public String getValue(Resident resident) {
            return valueProvider.apply(resident);
        }
    }

    @Override
    public LinkedHashMap<String, Object> serialize() {
        if (hasSettlement() && getSettlement().getId().isNull()) {
            throw new IllegalArgumentException("Resident had settlement but settlement has no ID?");
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("id", getId().value());
        map.put("uuid", getUUID().toString());
        map.put("name", getName());
        map.put("settlementId", getSettlement() != null ? getSettlement().getId().value() : null);
        map.put("settlementJoin", getSettlementJoin());
        map.put("rank", getSettlementRank().name());
        map.put("title", settlementTitle);
        map.put("lastLogin", getLastLogin());
        map.put("taxExempted", isTaxExempted());
        map.put("taxDebtAction", getTaxDebtAction().name());
        map.put("taxPaid", getTaxPaid());
        map.put("donated", getDonated());
        map.put("missionPoints", getMissionPoints());
        map.put("bonusClaims", getBonusClaims());
        map.put("friends", getFriends().stream().map(f -> f.getPlayerId().toString()).collect(Collectors.toList()));
        map.put("friendRequests", getFriendRequests());
        return map;
    }

    public static Resident deserialize(Map<String, Object> map) {
        if (map == null || map.get("uuid") == null || map.get("name") == null) {
            SettlementsPlugin.getInstance().getLogging().warn("Could not deserialize Resident! (" + map + ")");
            return null;
        }
        Resident resident = new Resident(UUID.fromString((String) map.get("uuid")), (String) map.get("name"));
        resident.setId(IdObject.get(map.get("id")));
        if (resident.getId().isNull()) {
            resident.setId(IdObject.get(map.get("_id")));
        }
        try {
            resident.setSettlementRank(SettlementRank.valueOf((String) map.get("rank")));
        } catch (IllegalArgumentException e) {
            SettlementsPlugin.getInstance().getLogging().warn("Invalid settlement rank for resident " + resident.getName() + "/" + resident.getUUID() + ": " + map.get("rank"));
        }
        if (map.get("settlementName") != null) {
            resident.setSettlement(SettlementManager.getInstance().getFromName((String) map.get("settlementName")));
        } else {
            resident.setSettlement(SettlementManager.getInstance().getFromId(IdObject.get(map.get("settlementId"))));
        }
        resident.setSettlementTitle((String) map.get("title"));
        resident.setSettlementJoin(Utils.getLong(map.get("settlementJoin")));
        resident.setLastLogin(Utils.getLong(map.get("lastLogin")));
        if (map.containsKey("taxExempted")) {
            resident.setTaxExempted(Utils.getBoolean(map.get("taxExempted")));
        }
        try {
            resident.setTaxDebtAction(TaxDebtAction.valueOf((String) map.get("taxDebtAction")));
        } catch (IllegalArgumentException e) {
            SettlementsPlugin.getInstance().getLogging().warn("Invalid tax debt action for resident " + resident.getName() + "/" + resident.getUUID() + ": " + map.get("taxDebtAction"));
        }
        resident.taxPaid = Utils.getLong(map.get("taxPaid"));
        resident.donated = Utils.getLong(map.get("donated"));
        resident.setMissionPoints(Utils.getLong(map.get("missionPoints")));
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
                        resident.addMissionData(md);
                    }
                });
        if (map.containsKey("bonusClaims")) {
            resident.setBonusClaims((Integer) map.get("bonusClaims"));
        }
        if (map.containsKey("friends")) {
            ((Map<UUID, String>) map.get("friends")).forEach(resident::addFriend);
        }
        if (map.containsKey("friendRequests")) {
            ((Collection<FriendRequest>) map.get("friendRequests")).forEach(resident::addFriendRequest);
        }
        return resident;
    }

    public IdObject getId() {
        return id;
    }

    @Override
    public void setId(IdObject id) {
        if (id == null) {
            id = IdObject.get(null);
        }
        this.id = id;
    }

    public void tell(Component message, String... replacements) {
        MessageUtils.tell(this.getPlayer(), ComponentUtils.parsePlaceholders(message, replacements));
    }

    public void tell(String message, String... replacements) {
        MessageUtils.tell(this.getPlayer(), ComponentUtils.parseComponent(message, replacements));
    }

    public boolean hasAllies() {
        return hasSettlement() && getSettlement().hasAllies();
    }

    public boolean isNotifyEnabled() {
        return notifyEnabled;
    }

    public void setNotifyEnabled(boolean notifyEnabled) {
        this.notifyEnabled = notifyEnabled;
    }

    public boolean isReceivingCloseBuildingNotification(Settlement settlement) {
        return !settlement.equals(getSettlement()) && closeBuildingNotificationToggles.getIfPresent(settlement.getId()) == null;
    }

    /**
     * Toggle whether to receive notifications about a certain settlement
     * @param settlement The settlement
     * @return Whether the resident will now be receiving notifications
     */
    public boolean toggleCloseBuildingNotification(Settlement settlement) {
        if (isReceivingCloseBuildingNotification(settlement)) {
            closeBuildingNotificationToggles.put(settlement.getId(), false);
            return false;
        } else {
            closeBuildingNotificationToggles.invalidate(settlement.getId());
            return true;
        }
    }

    @Override
    public int hashCode() {
        return uuid.hashCode() * 31 + (id == null ? 0 : id.hashCode());
    }

    @Override
    public String name() {
        return "Resident";
    }
}
