package de.themoep.settlements.db;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.ChatColorType;
import de.themoep.settlements.invites.Invite;
import de.themoep.settlements.invites.SettlementInvite;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.AllyRequest;
import de.themoep.settlements.land.Banished;
import de.themoep.settlements.land.JoinRequest;
import de.themoep.settlements.land.LandManager;

import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.Unbanished;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Friend;
import de.themoep.settlements.resident.FriendRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.IdObject;
import de.themoep.settlements.utils.Targetable;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class SqlDatabaseManager extends DatabaseManager {
    protected String tablePrefix;
    protected final SettlementsPlugin plugin = SettlementsPlugin.getInstance();

    private SaveQueue<Consumer<Connection>> logQueue;
    private SaveQueue<Settlement> settlementQueue;
    private SaveQueue<Resident> residentQueue;
    private SaveQueue<Resident> residentMissionDataQueue;
    private SaveQueue<OwnedLand> landQueue;
    private SaveQueue<Invite<?, ?>> inviteQueue;

    @Override
    public void connect() throws Exception {
        tablePrefix = Config.Storage.TABLE_PREFIX;

        int queueSize = Config.Storage.Queue.SIZE;
        int queueWait = Config.Storage.Queue.WAIT;
        int missionQueueSize = Config.Storage.Queue.Missions.SIZE;
        int missionQueueWait = Config.Storage.Queue.Missions.WAIT;

        logQueue = new SaveQueue<>("log", this, queueSize, queueWait, (conn, consumer) -> consumer.accept(conn));
        settlementQueue = new SaveQueue<>("settlement", this, queueSize, queueWait, this::saveSettlement);
        residentMissionDataQueue = new SaveQueue<>("resident mission data", this, missionQueueSize, missionQueueWait * 20, this::saveResidentMissionData);
        residentQueue = new SaveQueue<>("resident", this, queueSize, queueWait, this::saveResident);
        landQueue = new SaveQueue<>("land", this, queueSize, queueWait, this::saveLand);
        inviteQueue = new SaveQueue<>("invite", this, queueSize, queueWait, this::saveInvite);

        initializeTables();
    }

    protected abstract Connection getConnection();

    protected abstract void closeConnection() throws SQLException;

    protected abstract void close(Connection conn);

    protected abstract void initializeTables() throws SQLException;

    protected abstract String getInsertIgnore();

    protected abstract PreparedStatement executeSerializableInsert(Connection conn, String tableName, DatabaseSerializable serializable, Map<String, Object> values) throws SQLException;

    protected abstract void executeDataMapUpdate(Connection conn, String tableName, String keyColumn, int keyId, String mapKey, Map<String, ?> values, Set<String> columns, String... mapValueColumns) throws SQLException;

    // Implement DatabaseManager methods

    @Override
    public void addNewSettlement(Settlement settlement) {
        Utils.catchSync();
        Utils.catchSync();
        Connection conn = getConnection();
        try {
            saveSettlement(conn, settlement);
        } finally {
            close(conn);
        }
    }

    @Override
    public void saveSettlement(Settlement settlement) {
        settlementQueue.add(settlement.getId(), settlement);
        settlementQueue.finishAdding();
    }

    private void saveSettlement(Connection conn, Settlement settlement) {
        try {
            if (!save(conn, settlement, "residents", "permissions", "missiondata", "npcs", "allyRequests", "joinRequests").isNull() && !settlement.getId().isNull()) {
                updateDataMap(conn, "settlement_vaults", "settlementId", (Integer) settlement.getId().value(), "content", "amount", settlement.getVault());
                updateDataMap(conn, "settlement_outposts", "settlementId", (Integer) settlement.getId().value(), "name", "", settlement.getOutpostHomes());
                updateDataMap(conn, "settlement_uses", "settlementId", (Integer) settlement.getId().value(), "type", "state",
                        settlement.getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                updateDataMap(conn, "settlement_perks", "settlementId", (Integer) settlement.getId().value(), "perk", "",
                        settlement.getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
                updateDataMap(conn, "settlement_permissions", "settlementId", (Integer) settlement.getId().value(), "rank", "permission", settlement.getRanksPermissions(), "value");
                updateDataMap(conn, "settlement_titles", "settlementId", (Integer) settlement.getId().value(), "rank", "title", settlement.getRankTitles());
                updateDataMap(conn, "settlement_defaulttitles", "settlementId", (Integer) settlement.getId().value(), "rank", "title", settlement.getDefaultRankTitles());
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving settlement " + settlement.getName() + " to database!", e);
        }
    }

    @Override
    public void saveSettlements(Collection<Settlement> settlements) {
        for (Settlement settlement : settlements) {
            settlementQueue.add(settlement.getId(), settlement);
        }
        settlementQueue.finishAdding();
    }

    @Override
    public void saveMissionData(Resident resident) {
        residentMissionDataQueue.add(resident.getId(), resident);
        residentMissionDataQueue.finishAdding();
    }

    @Override
    public void saveResidentMissionData(Collection<Resident> residents) {
        for (Resident resident : residents) {
            residentMissionDataQueue.add(resident.getId(), resident);
        }
        residentMissionDataQueue.finishAdding();
    }

    public void saveResidentMissionData(Connection conn, Resident resident) {
        Utils.catchSync();
        try {
            updateDataMap(conn, "resident_missiondata", "userId", (Integer) resident.getId().value(), "mission", "type", resident.getMissionDataMap(), "progress");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving mission data for resident " + resident.getName() + " to database!", e);
        }
    }

    @Override
    public void deleteSettlement(Settlement settlement) {
        Utils.catchSync();
        settlementQueue.remove(settlement.getId(), settlement);
        Connection conn = getConnection();
        if (settlement.getId() != null && !settlement.getId().isNull()) {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "settlements WHERE id=?")) {
                sta.setInt(1, (Integer) settlement.getId().value());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + settlement.getId().value() + "/" + settlement.getName() + " from database!", e);
            } finally {
                close(conn);
            }
        } else {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "settlements WHERE name=?")) {
                sta.setString(1, settlement.getName());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + settlement.getId().value() + "/" + settlement.getName() + " from database!", e);
            } finally {
                close(conn);
            }
        }
    }

    @Override
    public void delete(Settlement landOwner, Ally ally) {
        Utils.catchSync();
        Connection conn = getConnection();
        if (ally.getId() != null && !ally.getId().isNull()) {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + landOwner.getDbName() + "_allies WHERE id=?")) {
                sta.setInt(1, (Integer) ally.getId().value());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + landOwner.getDbName() + " " + landOwner.getName() + " ally " + ally + " from database!", e);
            } finally {
                close(conn);
            }
        } else {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + landOwner.getDbName() + "_allies WHERE " + landOwner.getDbName() + "Id = ?, allies_id = ? LIMIT 1")) {
                sta.setInt(1, (Integer) landOwner.getId().value());
                sta.setInt(2, (Integer) ally.getId().value());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + landOwner.getDbName() + " " + landOwner.getName() + " ally " + ally + " from database!", e);
            } finally {
                close(conn);
            }
        }
    }

    private <T extends Targetable & DatabaseSerializable, S extends Targetable & DatabaseSerializable> void saveInvite(Connection conn, Invite<T,S> invite) {
        try {
            save(conn, invite);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving invite " + invite + " to database!", e);
        }
    }

    @Override
    public <T extends Targetable & DatabaseSerializable, S extends Targetable & DatabaseSerializable> void save(Invite<T,S> invite) {
        inviteQueue.add(invite);
        inviteQueue.finishAdding();
    }

    @Override
    public <T extends Targetable & DatabaseSerializable, S extends Targetable & DatabaseSerializable> void delete(Invite<T,S> invite) {
        Utils.catchSync();
        inviteQueue.remove(invite.getId(), invite);
        Connection conn = getConnection();
        if (invite.getId() != null && !invite.getId().isNull()) {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "invites WHERE id=?")) {
                sta.setInt(1, (Integer) invite.getId().value());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete invite " + invite.getId().value() + " from database!", e);
            } finally {
                close(conn);
            }
        } else {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "invites WHERE type = ? AND targetType = ? AND targetId = ? AND sourceType = ? AND sourceId = ? LIMIT 1")) {
                sta.setString(1, invite.name());
                sta.setString(2, invite.getTargetType());
                sta.setInt(3, (Integer) invite.getTargetId().value());
                sta.setString(4, invite.getSourceType());
                sta.setInt(5, (Integer) invite.getSourceId().value());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete invite " + invite + " from database!", e);
            } finally {
                close(conn);
            }
        }

    }

    @Override
    public void unclaimLand(OwnedLand... lands) {
        Utils.catchSync();
        if (lands.length == 0) {
            return;
        }
        Connection conn = getConnection();
        try (PreparedStatement idSta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE id = ?;");
             PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE world = ? AND x = ? AND z = ?;")) {
            for (OwnedLand land : lands) {
                landQueue.remove(land.getId(), land);
                if (land.getId().isNull()) {
                    sta.setString(1, land.getWorldName());
                    sta.setInt(2, land.getX());
                    sta.setInt(3, land.getZ());
                    sta.addBatch();
                } else {
                    idSta.setObject(1, land.getId().value());
                    idSta.addBatch();
                }
            }
            idSta.executeBatch();
            sta.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + lands.length + " claims from database!", e);
        } finally {
            close(conn);
        }
    }

    @Override
    public void unclaimLand(Settlement landOwner) {
        Utils.catchSync();
        if (landOwner.getId().value() == null) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims of " + landOwner.getDbName() + " " + landOwner.getName() + " from database! It has no ID?");
            return;
        }

        for (OwnedLand land : LandManager.getInstance().getClaimedLand(landOwner)) {
            landQueue.remove(land.getId(), land);
        }

        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE landOwnerType = ? AND landOwnerId = ?;")) {
            sta.setString(1, landOwner.getDbName());
            sta.setInt(2, (Integer) landOwner.getId().value());
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims of " + landOwner.getDbName() + " " + landOwner.getName() + " from database! ", e);
        } finally {
            close(conn);
        }
    }

    @Override
    public void unclaimLand(String worldName) {
        Utils.catchSync();

        for (OwnedLand land : LandManager.getInstance().getWorldLand(worldName)) {
            landQueue.remove(land.getId(), land);
        }

        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE world = ?;")) {
            sta.setString(1, worldName);
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims of world " + worldName + " from database! ", e);
        } finally {
            close(conn);
        }
    }

    @Override
    public void renameLandWorld(String oldName, String newName) {
        Utils.catchSync();

        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement("UPDATE " + tablePrefix + "claims SET world = ? WHERE world = ?;")) {
            sta.setString(1, newName);
            sta.setString(2, oldName);
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to change claims world from " + oldName + " to " + newName + " in database! ", e);
        } finally {
            close(conn);
        }
    }

    @Override
    public void saveLand(OwnedLand... lands) {
        for (OwnedLand land : lands) {
            landQueue.add(land.getId(), land);
        }
        landQueue.finishAdding();
    }

    private void saveLand(Connection conn, OwnedLand land) {
        Utils.catchSync();
        try {
            if (!save(conn, land, "landOwner", "uses", "modes").isNull() && !land.getId().isNull()) {
                updateDataMap(conn, "claim_uses", "claimId", (Integer) land.getId().value(), "type", "state",
                        land.getUseStates().entrySet().stream().filter(e -> e.getValue() != null).collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                updateDataMap(conn, "claim_modes", "claimId", (Integer) land.getId().value(), "mode", "enabled",
                        land.getModes().entrySet().stream()
                                .filter(e -> e.getKey().appliesTo(PlotMode.AppliesTo.LAND_OWNER))
                                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)));

                updateUserList(conn, "claim_members", "claimId", (Integer) land.getId().value(), "memberId", land.getPlotMembers());
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving claim " + land.getKey() + " to database!", e);
        }
    }

    @Override
    public Collection<OwnedLand> getAllLand() {
        Utils.catchSync();
        String sql = "SELECT c.*, r.uuid AS `owner` FROM " + tablePrefix + "claims AS c LEFT JOIN " + tablePrefix + "residents AS r ON c.ownerId = r.id;";
        Connection conn = getConnection();
        try ( ResultSet rs = conn.createStatement().executeQuery(sql)) {
            List<OwnedLand> claims = new ArrayList<>();
            while (rs.next()) {
                OwnedLand ownedLand = createLand(conn, rs);
                if (ownedLand != null) {
                    claims.add(ownedLand);
                }
            }
            return claims;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all claims from database!", e);
        } finally {
            close(conn);
        }
        return Collections.emptyList();
    }

    @Override
    public void deleteLand(IdObject idObject) {
        Utils.catchSync();
        String sql = "DELETE FROM " + tablePrefix + "claims WHERE id = ?;";
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, (Integer) idObject.value());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims " + idObject + " from database!", e);
        } finally {
            close(conn);
        }
    }

    @Override
    public Collection<Settlement> getAllSettlements() {
        Utils.catchSync();
        String sql = "SELECT s.*, r.uuid AS chief FROM " + tablePrefix + "settlements AS s LEFT JOIN " + tablePrefix + "residents AS r ON s.chiefId = r.id;";
        Connection conn = getConnection();
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            List<Settlement> settlements = new ArrayList<>();
            while (rs.next()) {
                Settlement settlement = createSettlement(conn, rs);
                if (settlement != null) {
                    settlements.add(settlement);
                }
            }
            return settlements;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all settlements from database!", e);
        } finally {
            close(conn);
        }
        return Collections.emptyList();
    }

    public Collection<SettlementInvite> getSettlementInvites() {
        Utils.catchSync();
        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "invites WHERE type = ?;")) {
            sta.setString(1, "settlementinvite");
            ResultSet rs = sta.executeQuery();
            List<SettlementInvite> invites = new ArrayList<>();
            while (rs.next()) {
                SettlementInvite invite = SettlementInvite.deserialize(toMap(rs));
                if (invite != null) {
                    invites.add(invite);
                }
            }
            return invites;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all settlements from database!", e);
        } finally {
            close(conn);
        }
        return Collections.emptyList();
    }

    @Override
    public Resident getResident(int id) {
        String sql = "SELECT * FROM " + tablePrefix + "residents WHERE id=?";
        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setInt(1, id);
            ResultSet rs = sta.executeQuery();
            if (rs.next()) {
                return createResident(conn, rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get resident information for player with ID " + id + " from database!", e);
        } finally {
            close(conn);
        }
        return null;
    }

    @Override
    public Resident getResident(UUID uuid) {
        String sql = "SELECT * FROM " + tablePrefix + "residents WHERE uuid=?";
        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setString(1, uuid.toString());
            ResultSet rs = sta.executeQuery();
            if (rs.next()) {
                return createResident(conn, rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get resident information for player with UUID " + uuid + " from database!", e);
        } finally {
            close(conn);
        }
        return null;
    }

    @Override
    public Resident getResident(String name) {
        if (!name.matches("^\\w{3,16}$")) {
            return null;
        }
        String sql = "SELECT * FROM " + tablePrefix + "residents WHERE name LIKE ?";
        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setString(1, name.replace("_", "\\_"));
            ResultSet rs = sta.executeQuery();
            if (rs.next()) {
                return createResident(conn, rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get resident information for player with name " + name + " from database!", e);
        } finally {
            close(conn);
        }
        return null;
    }

    @Override
    public void saveResidents(Collection<Resident> residents) {
        for (Resident resident : residents) {
            residentQueue.add(IdObject.get(resident.getId()), resident);
        }
        residentQueue.finishAdding();
    }

    @Override
    public void saveResident(Resident resident) {
        residentQueue.add(IdObject.get(resident.getId()), resident);
        residentQueue.finishAdding();
    }

    @Override
    public void saveResidentNow(Resident resident) {
        residentQueue.remove(resident);
        Connection conn = getConnection();
        try {
            saveResident(conn, resident);
        } finally {
            close(conn);
        }
    }

    private void saveResident(Connection conn, Resident resident) {
        if (resident == null) {
            return;
        }
        Utils.catchSync();
        Map<String, Object> values = getValues(conn, resident.serialize());
        try {
            boolean hadId = resident.getId() != null && !resident.getId().isNull();
            PreparedStatement sta = executeSerializableInsert(conn, "residents", resident, values);
            if (!hadId) {
                ResultSet rs = sta.getGeneratedKeys();
                if (rs.next()) {
                    resident.setId(IdObject.get(rs.getInt(1)));
                    ResidentManager.getInstance().addResident(resident);
                }
            }
            if (resident.getId() != null && !resident.getId().isNull()) {
                updateList(conn, "resident_channels", "userId", (Integer) resident.getId().value(), "channel", resident.getChannels().stream().map(ChatChannel::name).collect(Collectors.toList()));
                updateUserList(conn, "resident_friends", "userId", (Integer) resident.getId().value(), "friendId", resident.getFriends().stream().map(Friend::getPlayerId).collect(Collectors.toList()));
                updateDataMap(conn, "resident_channel_colors", "userId", (Integer) resident.getId().value(), "channel", "color", Arrays.stream(ChatChannel.values())
                        .filter(c -> resident.getChannelColor(c, ChatColorType.MESSAGE) != null)
                        .collect(Collectors.toMap(Enum::name, c -> NamedTextColor.nearestTo(resident.getChannelColor(c, ChatColorType.MESSAGE)).toString())));
                updateDataMap(conn, "resident_channel_prefix_colors", "userId", (Integer) resident.getId().value(), "channel", "color", Arrays.stream(ChatChannel.values())
                        .filter(c -> resident.getChannelColor(c, ChatColorType.PREFIX) != null)
                        .collect(Collectors.toMap(Enum::name, c -> NamedTextColor.nearestTo(resident.getChannelColor(c, ChatColorType.PREFIX)).toString())));
                if (!hadId) {
                    ResidentManager.getInstance().addResident(resident);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving player " + resident.getName() + "/" + resident.getUUID() + " to database!", e);
        }
    }

    @Override
    public void log(Settlement settlement, Resident resident, String action, Map<String, Long> values) {
        if (values.isEmpty()) {
            return;
        }
        if (settlement != null && (settlement.getId() == null || settlement.getId().isNull())) {
            return;
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logQueue.add(IdObject.get(timestamp.getTime()), conn -> log(conn, timestamp, settlement, resident, action, values));
    }

    private void log(Connection conn, Timestamp timestamp, Settlement settlement, Resident resident, String action, Map<String, Long> values) {
        try (PreparedStatement sta = conn.prepareStatement("INSERT INTO " + tablePrefix + "logs " +
                     "(`settlementId`, `residentId`, `action`, `type`, `amount`, `timestamp`) " +
                     "VALUES (?, ?, ?, ?, ?, ?);"
             )
        ) {
            for (Map.Entry<String, Long> value : values.entrySet()) {
                if (value.getValue() != 0) {
                    sta.setInt(1, settlement != null ? (Integer) settlement.getId().value() : -1);
                    sta.setInt(2, resident != null && (resident.getId() != null && !resident.getId().isNull()) ? (Integer) resident.getId().value() : -1);
                    sta.setString(3, action.toLowerCase());
                    sta.setString(4, value.getKey().toLowerCase());
                    sta.setLong(5, value.getValue());
                    sta.setTimestamp(6, timestamp);
                    sta.addBatch();
                }
            }
            sta.executeBatch();
        } catch (SQLException e) {
            SettlementsPlugin.getInstance().getLogging().exception(e);
        }
    }

    @Override
    public int getLogPageCount(Settlement settlement, Resident resident, LogType type) {
        return getLogPageCount("settlementId", settlement.getId(), resident, type);
    }

    private int getLogPageCount(String column, IdObject id, Resident resident, LogType type) {
        if (id == null || id.isNull()) {
            return 0;
        }
        String sql = "SELECT count(l.id) " +
                "FROM " + tablePrefix + "logs AS l " +
                "WHERE l." + column + "=?";
        switch (type) {
            case BANK -> sql += " AND l.type = 'cash'";
            case VAULT -> sql += " AND l.type != 'cash' AND l.type != 'claim'";
        }
        if (!resident.getSettlementRank().atLeast(SettlementRank.getChief())) {
            sql += " AND (l.action = 'deposit' OR l.action = 'donate')";
        }
        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setInt(1, (Integer) id.value());
            ResultSet rs = sta.executeQuery();
            if (rs.next()) {
                return (int) Math.ceil(rs.getInt(1) / (double) Config.ENTRIES_PER_PAGE);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get log page count for " + id, e);
        } finally {
            close(conn);
        }
        return 0;
    }

    @Override
    public void disable() {
        residentMissionDataQueue.forceSave();
        settlementQueue.forceSave();
        residentQueue.forceSave();
        landQueue.forceSave();
        try {
            closeConnection();
        } catch (SQLException e) {
            SettlementsPlugin.getInstance().getLogging().exception(e);
        }
    }

    @Override
    public List<String[]> getLog(Settlement landOwner, Resident resident, int page, LogType type) {
        String column = landOwner.getDbName() + "Id";
        List<String[]> log = new ArrayList<>();
        if (landOwner.getId() == null || landOwner.getId().isNull() || page < 1) {
            return log;
        }
        int start = (page-1) * Config.ENTRIES_PER_PAGE;
        String sql = "SELECT l.settlementId, l.action, l.type, l.amount, l.timestamp, r.name AS playerName, r.uuid AS playerId, r.rank AS playerRank, r.title AS playerTitle " +
                "FROM " + tablePrefix + "logs AS l " +
                "LEFT JOIN " + tablePrefix + "residents AS r ON l.residentId = r.id " +
                "WHERE l." + column + "=?";
        switch (type) {
            case BANK -> sql += " AND l.type = 'cash'";
            case VAULT -> sql += " AND l.type != 'cash' AND l.type != 'claim'";
        }
        if (!resident.getSettlementRank().atLeast(SettlementRank.getChief())) {
            sql += " AND (l.action = 'deposit' OR l.action = 'donate')";
        }
        sql += " ORDER BY l.id DESC LIMIT " + start + "," + Config.ENTRIES_PER_PAGE + ";";
        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setInt(1, (Integer) landOwner.getId().value());
            ResultSet rs = sta.executeQuery();
            while (rs.next()) {
                int amount = rs.getInt("amount");
                if (amount == 0) {
                    continue;
                }
                boolean hasPlayer = rs.getString("playerId") != null;
                SettlementRank rank = SettlementRank.getDefault();
                if (hasPlayer) {
                    try {
                        rank = SettlementRank.valueOf(rs.getString("playerRank").toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }

                Settlement settlement = null;
                int settlementId = rs.getInt("settlementId");
                if (settlementId > -1) {
                    settlement = SettlementManager.getInstance().getFromId(IdObject.get(settlementId));
                }

                String title = hasPlayer ? rs.getString("playerTitle") : "";
                if (title == null) {
                    title = rank.getTag(settlement);
                }
                log.add(new String[] {
                        "settlement", settlement != null ? settlement.getName() : settlementId > -1 ? "id:" + settlementId : "",
                        "settlementstate", settlement != null ? settlement.getState().getTag() : settlementId > -1 ? "Unknown" : "",
                        "name", hasPlayer ? rs.getString("playerName") : "",
                        "uuid", hasPlayer ? rs.getString("playerId") : "",
                        "rank", hasPlayer ? rank.getTag(settlement) : "",
                        "title", title,
                        "action", rs.getString("action") != null ? rs.getString("action") : "",
                        "type", rs.getString("type") != null ? rs.getString("type") : "?",
                        "icon", "",
                        "amount", String.valueOf(amount),
                        "timestamp", Utils.TIME_FORMAT.format(new Date(rs.getTimestamp("timestamp").getTime()))
                });
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get log for " + landOwner.getDbName() + "/" + landOwner.getId(), e);
        } finally {
            close(conn);
        }
        return log;
    }

    // ------ Creation methods ----- //

    private Settlement createSettlement(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Map<String, Object> map = toMap(rs);

        PreparedStatement rSta = conn.prepareStatement("SELECT id, uuid FROM " + tablePrefix + "residents WHERE settlementId = ?;");
        rSta.setInt(1, id);
        ResultSet rRs = rSta.executeQuery();
        Collection<String> residents = new LinkedHashSet<>();
        while (rRs.next()) {
            residents.add(rRs.getString("uuid"));
        }
        map.put("residents", residents);

        map.put("joinRequests", getJoinRequests(conn, "settlement", id));
        map.put("allies", selectDeserializableList(conn, "settlement", id, "allies", Ally.class));
        map.put("allyRequests", getAllyRequests(conn, "settlement", id));
        map.put("banished", selectDeserializableList(conn, "settlement", id, "banished", Banished.class));
        map.put("unbanished", selectDeserializableList(conn, "settlement", id, "unbanished", Unbanished.class));

        map.put("uses", selectMap(conn, "settlement_uses", "settlementId", id, "type", "state", String.class));
        map.put("vault", selectMap(conn, "settlement_vaults", "settlementId", id, "content", "amount", Long.class));
        map.put("defaulttitles", selectMap(conn, "settlement_defaulttitles", "settlementId", id, "rank", "title", String.class));

        PreparedStatement oSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_outposts WHERE settlementId = ?;");
        oSta.setInt(1, id);
        ResultSet oRs = oSta.executeQuery();
        Map<String, Map<String, Object>> outposts = new LinkedHashMap<>();
        while (oRs.next()) {
            outposts.put(oRs.getString("name").toLowerCase(), toMap(oRs));
        }
        map.put("outposts", outposts);

        PreparedStatement pSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_perks WHERE settlementId = ?;");
        pSta.setInt(1, id);
        ResultSet pRs = pSta.executeQuery();
        Map<String, Map<String, Object>> unlockedPerks = new LinkedHashMap<>();
        while (pRs.next()) {
            unlockedPerks.put(pRs.getString("perk").toLowerCase(), toMap(pRs));
        }
        map.put("perks", unlockedPerks);

        PreparedStatement rpSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_permissions WHERE settlementId = ?;");
        rpSta.setInt(1, id);
        ResultSet rpRs = rpSta.executeQuery();
        Map<String, Map<String, Boolean>> rankPermissions = new LinkedHashMap<>();
        while (rpRs.next()) {
            rankPermissions.computeIfAbsent(rpRs.getString("rank").toLowerCase(), k -> new LinkedHashMap<>())
                    .put(rpRs.getString("permission"), rpRs.getBoolean("value"));
        }
        map.put("permissions", rankPermissions);

        PreparedStatement mdSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_missiondata WHERE settlementId = ?;");
        mdSta.setInt(1, id);
        ResultSet mdRs = mdSta.executeQuery();
        Map<String, Map<String, Long>> missionData = new LinkedHashMap<>();
        while (mdRs.next()) {
            missionData.computeIfAbsent(mdRs.getString("mission").toLowerCase(), k -> new LinkedHashMap<>())
                    .put(mdRs.getString("type"), mdRs.getLong("progress"));
        }
        map.put("missiondata", missionData);

        PreparedStatement tpSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_titles WHERE settlementId = ?;");
        tpSta.setInt(1, id);
        ResultSet tpRs = tpSta.executeQuery();
        Map<String, Collection<String>> rankTitles = new LinkedHashMap<>();
        while (tpRs.next()) {
            rankTitles.computeIfAbsent(tpRs.getString("rank").toLowerCase(), k -> new LinkedHashSet<>()).add(tpRs.getString("title"));
        }
        map.put("titles", rankTitles);


        return Settlement.deserialize(map);
    }

    private Collection<AllyRequest> getAllyRequests(Connection conn, String type, int id) {
        Utils.catchSync();
        String sql = "SELECT *" +
                " FROM " + tablePrefix + "invites" +
                " WHERE type = ? AND targetType = ? AND targetId = ?;";
        try (PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setString(1, "allyrequest");
            sta.setString(2, type);
            sta.setInt(3, id);
            ResultSet rs = sta.executeQuery();
            List<AllyRequest> requests = new ArrayList<>();
            while (rs.next()) {
                AllyRequest request = AllyRequest.deserialize(toMap(rs));
                if (request != null) {
                    requests.add(request);
                }
            }
            return requests;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all ally requests of " + type + " " + id + " from database!", e);
        }
        return Collections.emptyList();
    }

    private Collection<JoinRequest<?>> getJoinRequests(Connection conn, String type, int id) {
        Utils.catchSync();
        String sql = "SELECT *" +
                " FROM " + tablePrefix + "invites" +
                " WHERE type = ? AND targetType = ? AND targetId = ?;";
        try (PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setString(1, "joinrequest");
            sta.setString(2, type);
            sta.setInt(3, id);
            ResultSet rs = sta.executeQuery();
            List<JoinRequest<?>> requests = new ArrayList<>();
            while (rs.next()) {
                JoinRequest<?> request = JoinRequest.deserialize(toMap(rs));
                requests.add(request);
            }
            return requests;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all join requests of " + type + " " + id + " from database!", e);
        }
        return Collections.emptyList();
    }

    private Resident createResident(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        int id = rs.getInt("id");
        map.put("channels", selectList(conn, "resident_channels", "userId", id, "channel"));
        map.put("friends", selectResidentsMap(conn, "resident_friends", "userId", id, "friendId"));
        map.put("friendRequests", getFriendRequests(conn, id));
        map.put("ignores", selectResidentList(conn, "resident_ignores", "userId", id, "ignoredId"));
        map.put("channelColors", selectMap(conn, "resident_channel_colors", "userId", id, "channel", "color", String.class));
        map.put("channelPrefixColors", selectMap(conn, "resident_channel_prefix_colors", "userId", id, "channel", "color", String.class));

        PreparedStatement mdSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "resident_missiondata WHERE userId = ?;");
        mdSta.setInt(1, id);
        ResultSet mdRs = mdSta.executeQuery();
        Map<String, Map<String, Long>> missionData = new LinkedHashMap<>();
        while (mdRs.next()) {
            missionData.computeIfAbsent(mdRs.getString("mission").toLowerCase(), k -> new LinkedHashMap<>())
                    .put(mdRs.getString("type"), mdRs.getLong("progress"));
        }
        map.put("missiondata", missionData);
        return Resident.deserialize(map);
    }

    private Collection<FriendRequest> getFriendRequests(Connection conn, int id) {
        Utils.catchSync();
        String sql = "SELECT i.targetId AS targetId, i.sourceId AS sourceId, i.time AS time, r.uuid as senderUuid, r.name as senderName" +
                " FROM " + tablePrefix + "invites AS i" +
                " JOIN " + tablePrefix + "residents AS r ON i.sourceId = r.id" +
                " WHERE i.type = ? AND i.targetId = ?;";
        try (PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setString(1, "friendrequest");
            sta.setInt(2, id);
            ResultSet rs = sta.executeQuery();
            List<FriendRequest> requests = new ArrayList<>();
            while (rs.next()) {
                FriendRequest request = FriendRequest.deserialize(toMap(rs));
                if (request != null) {
                    requests.add(request);
                }
            }
            return requests;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all friend requests of resident " + id + " from database!", e);
        }
        return Collections.emptyList();
    }

    private OwnedLand createLand(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        int id = rs.getInt("id");
        map.put("modes", selectMap(conn, "claim_modes", "claimId", id, "mode", "enabled", Boolean.class));
        map.put("uses", selectMap(conn, "claim_uses", "claimId", id, "type", "state", String.class));
        map.put("plotMembers", selectResidentList(conn, "claim_members", "claimId", id, "memberId"));
        return OwnedLand.deserialize(map);
    }

    // ------ Database operation helpers ----- //

    private IdObject save(Connection conn, DatabaseSerializable serializable, String... ignored) throws SQLException {
        if (serializable == null) {
            SettlementsPlugin.getInstance().getLogging().error("Tried to save something to the database but it was null?");
            SettlementsPlugin.getInstance().getLogging().exception(new NullPointerException().fillInStackTrace());
            return IdObject.get(null);
        }
        Map<String, Object> serialized = serializable.serialize();
        Map<String, Object> values = getValues(conn, serialized, ignored);
        PreparedStatement sta = executeSerializableInsert(conn, serializable.getDbName() + "s", serializable, values);
        if (serializable.getId() == null || serializable.getId().isNull()) {
            ResultSet rs = sta.getGeneratedKeys();
            if (rs.next()) {
                serializable.setId(IdObject.get(rs.getInt(1)));
            }
        }
        if (serializable.getId() != null && !serializable.getId().isNull()) {
            Set<String> ignoredSet = new HashSet<>(Arrays.asList(ignored));
            for (Map.Entry<String, Object> e : serialized.entrySet()) {
                if (!ignoredSet.contains(e.getKey())) {
                    if (e.getValue() instanceof Collection) {
                        updateList(conn, serializable.getDbName() + "_" + e.getKey(), serializable.getDbName() + "Id", (Integer) serializable.getId().value(), e.getKey(), (Collection<?>) e.getValue());
                    }
                }
            }
        }

        return serializable.getId();
    }

    private void updateDataMap(Connection conn, String tableName, String keyColumn, int keyId, String mapKey, String mapValue, Map<String, ?> values, String... mapValueColumns) throws SQLException {
        String deleteSql;
        Map<Integer, Object> deleteObjects = new HashMap<>();
        int i = 0;
        deleteObjects.put(++i, keyId);
        if (values.isEmpty()) {
            deleteSql = "DELETE FROM " + tablePrefix + tableName +
                    " WHERE " + keyColumn + " = ?;";
        } else if (!mapValue.isEmpty() && values.values().iterator().next() instanceof Collection) {
            Map<String, Collection<Object>> map = new HashMap<>();
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                Collection<Object> objects = (Collection<Object>) entry.getValue();
                if (!objects.isEmpty()) {
                    map.put(entry.getKey(), objects);
                }
            }

            deleteSql = "DELETE FROM " + tablePrefix + tableName +
                    " WHERE " + keyColumn + " = ?";

            if (!map.isEmpty()) {
                deleteSql += " AND (" + mapKey + " NOT IN " +
                        "(" + String.join(",", Collections.nCopies(map.size(), "?")) + ")";
                for (String key : map.keySet()) {
                    deleteObjects.put(++i, key);
                }
                List<String> rankQuery = new ArrayList<>();
                for (Map.Entry<String, Collection<Object>> entry : map.entrySet()) {
                    rankQuery.add(mapKey + " = ?" +
                            " AND " + mapValue + " NOT IN " +
                            "(" + String.join(",", Collections.nCopies(entry.getValue().size(), "?")) + ")");
                    deleteObjects.put(++i, entry.getKey());
                    for (Object o : entry.getValue()) {
                        deleteObjects.put(++i, o);
                    }
                }
                deleteSql += " OR ((" + String.join(") OR (", rankQuery) + ")))";
            }
            deleteSql += ";";
        } else if (!mapValue.isEmpty() && values.values().iterator().next() instanceof Map) {
            List<Object> keyObjects = new ArrayList<>();
            List<Object> valueObjects = new ArrayList<>();
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    for (Object value : ((Map<Object, Object>) entry.getValue()).keySet()) {
                        keyObjects.add(entry.getKey());
                        valueObjects.add(value);
                    }
                }
            }

            for (Object o : keyObjects) {
                deleteObjects.put(++i, o);
            }
            for (Object o : valueObjects) {
                deleteObjects.put(++i, o);
            }

            deleteSql = "DELETE FROM " + tablePrefix + tableName +
                    " WHERE " + keyColumn + " = ?";

            if (!keyObjects.isEmpty()) {
                deleteSql += " AND " + mapKey + " NOT IN (" + String.join(",", Collections.nCopies(keyObjects.size(), "?")) + ")";
            }
            if (!valueObjects.isEmpty()) {
                deleteSql += " AND " + mapValue + " NOT IN (" + String.join(",", Collections.nCopies(valueObjects.size(), "?")) + ")";
            }
            deleteSql += ";";

        } else {
            deleteSql = "DELETE FROM " + tablePrefix + tableName +
                    " WHERE " + keyColumn + " = ?" +
                    " AND " + mapKey + " NOT IN " +
                    "(" + String.join(",", Collections.nCopies(values.size(), "?")) + ");";
            for (String key : values.keySet()) {
                deleteObjects.put(++i, key);
            }
        }

        PreparedStatement deleteSta = conn.prepareStatement(deleteSql);
        for (Map.Entry<Integer, Object> entry : deleteObjects.entrySet()) {
            deleteSta.setObject(entry.getKey(), toDbType(entry.getValue()));
        }
        deleteSta.executeUpdate();

        if (!values.isEmpty()) {
            Object value = values.values().iterator().next();
            Set<String> columns = new LinkedHashSet<>();
            if (value instanceof ConfigurationSerializable serializable) {
                value = serializable.serialize();
            }
            if (value instanceof Map<?, ?> map && mapValueColumns.length == 0) {
                if ((map.keySet().iterator().next() instanceof String)) {
                    for (String column : getValues(conn, (Map<String, ?>) value, mapKey).keySet()) {
                        if (mapValue.isEmpty()) {
                            columns.add(column);
                        } else {
                            columns.add(mapValue + "_" + column);
                        }
                    }
                }
            }
            if (columns.isEmpty()) {
                if (!mapValue.isEmpty()) {
                    columns.add(mapValue);
                    Collections.addAll(columns, mapValueColumns);
                } else {
                    return;
                }
            }
            executeDataMapUpdate(conn, tableName, keyColumn, keyId, mapKey, values, columns, mapValueColumns);
        }
    }

    private <T> Map<String, T> selectMap(Connection conn, String table, String idColumn, int id, String keyColumn, String valueColumn, Class<T> valueClass) throws SQLException {
        PreparedStatement sta = conn.prepareStatement("SELECT * FROM " + tablePrefix + table + " WHERE " + idColumn + " = ?;");
        sta.setInt(1, id);
        ResultSet rs = sta.executeQuery();
        Map<String, T> map = new LinkedHashMap<>();
        while (rs.next()) {
            map.put(rs.getString(keyColumn), (T) rs.getObject(valueColumn));
        }
        return map;
    }

    private <D extends DatabaseSerializable> List<D> selectDeserializableList(Connection conn, String ownerType, int id, String valueType, Class<D> deserializable) throws SQLException {
        return selectDeserializableList(conn, ownerType + "_" + valueType, ownerType + "Id", id, valueType, deserializable);
    }

    private <D extends DatabaseSerializable> List<D> selectDeserializableList(Connection conn, String tableName, String keyColumn, int keyValue, String valueType, Class<D> deserializable) throws SQLException {
        try {
            Method deserialize = deserializable.getMethod("deserialize", Map.class);
            PreparedStatement sta = conn.prepareStatement("SELECT * FROM " + tablePrefix + tableName + " WHERE " + keyColumn + "=?;");
            sta.setInt(1, keyValue);
            ResultSet rs = sta.executeQuery();
            List<D> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> map = toMap(rs);
                try {
                    list.add((D) deserialize.invoke(null, map));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    SettlementsPlugin.getInstance().getLogging().warn("Unable to deserialize " + deserializable.getSimpleName() + " object from " + tableName + " with values " + map);
                    SettlementsPlugin.getInstance().getLogging().exception(e);
                }
            }
            return list;
        } catch (NoSuchMethodException e) {
            SettlementsPlugin.getInstance().getLogging().warn(deserializable.getSimpleName() + " class does not have static deserialize method?");
            SettlementsPlugin.getInstance().getLogging().exception(e);
        }
        return Collections.emptyList();
    }

    private List<Object> selectList(Connection conn, String ownerType, int id, String valueType) throws SQLException {
        return selectList(conn, ownerType + "_" + valueType, ownerType + "Id", id, valueType);
    }

    private List<Object> selectList(Connection conn, String tableName, String keyColumn, int keyValue, String valueColumn) throws SQLException {
        String sql = "SELECT " + valueColumn + " FROM " + tablePrefix + tableName + " WHERE " + keyColumn + "=?;";
        List<Object> list = new ArrayList<>();
        PreparedStatement sta = conn.prepareStatement(sql);
        sta.setInt(1, keyValue);
        ResultSet rs = sta.executeQuery();
        while (rs.next()) {
            list.add(rs.getObject(valueColumn));
        }
        return list;
    }

    private Collection<String> selectResidentList(Connection conn, String tableName, String keyColumn, int keyValue, String valueColumn) throws SQLException {
        String sql = "SELECT r.uuid FROM " + tablePrefix + tableName + " AS f" +
                " JOIN " + tablePrefix + "residents AS r ON f." + valueColumn + " = r.id" +
                " WHERE f." + keyColumn + "=?";
        Set<String> uuids = new LinkedHashSet<>();
        PreparedStatement sta = conn.prepareStatement(sql);
        sta.setInt(1, keyValue);
        ResultSet rs = sta.executeQuery();
        while (rs.next()) {
            uuids.add(rs.getString(1));
        }
        return uuids;
    }

    private Map<UUID, String> selectResidentsMap(Connection conn, String tableName, String keyColumn, int keyValue, String valueColumn) throws SQLException {
        String sql = "SELECT r.uuid AS uuid, r.name AS name FROM " + tablePrefix + tableName + " AS f" +
                " JOIN " + tablePrefix + "residents AS r ON f." + valueColumn + " = r.id" +
                " WHERE f." + keyColumn + "=?";
        Map<UUID, String> map = new LinkedHashMap<>();
        PreparedStatement sta = conn.prepareStatement(sql);
        sta.setInt(1, keyValue);
        ResultSet rs = sta.executeQuery();
        while (rs.next()) {
            map.put(Utils.parseUuid(rs.getString("uuid")), rs.getString("name"));
        }
        return map;
    }

    private void updateList(Connection conn, String tableName, String keyColumn, int keyId, String entryColumn, Collection<?> entries) throws SQLException {
        String deleteSql;
        if (entries.isEmpty()) {
            deleteSql = "DELETE FROM " + tablePrefix + tableName + " " +
                    " WHERE " + keyColumn + " = ?;";
        } else {
            String andSql;
            Object o = entries.iterator().next();
            if (o instanceof DatabaseSerializable) {
                andSql = " AND " + entryColumn + "_id NOT IN (" + String.join(",", Collections.nCopies(entries.size(), "?")) + ")";
            } else {
                andSql = " AND " + entryColumn + " NOT IN (" + String.join(",", Collections.nCopies(entries.size(), "?")) + ")";
            }
            deleteSql = "DELETE FROM " + tablePrefix + tableName + " " +
                    " WHERE " + keyColumn + " = ?" + andSql + ";";
        }

        PreparedStatement deleteSta = conn.prepareStatement(deleteSql);
        deleteSta.setInt(1, keyId);
        int i = 2;
        for (Object entry : entries) {
            if (entry instanceof DatabaseSerializable serializable) {
                deleteSta.setObject(i++, toDbType(serializable.getId()));
            } else {
                deleteSta.setObject(i++, toDbType(entry));
            }
        }
        deleteSta.executeUpdate();

        if (!entries.isEmpty()) {
            StringBuilder insertSql = new StringBuilder(getInsertIgnore() + " INTO " + tablePrefix + tableName + " (" + keyColumn); // TODO: Insert update
            Object o = entries.iterator().next();
            if (o instanceof DatabaseSerializable serializable) {
                Collection<String> keys = serializable.serialize().keySet();
                for (String key : keys) {
                    insertSql.append(", ").append(entryColumn).append("_").append(key);
                }
                insertSql.append(") VALUES (?, ").append(String.join(", ", Collections.nCopies(keys.size(), "?"))).append(");");
            } else {
                insertSql.append(", ").append(entryColumn).append(") VALUES (?,?);");
            }

            PreparedStatement insertSta = conn.prepareStatement(insertSql.toString());
            for (Object entry : entries) {
                insertSta.setInt(1, keyId);
                if (entry instanceof DatabaseSerializable serializable) {
                    int j = 2;
                    for (Map.Entry<String, Object> e : serializable.serialize().entrySet()) {
                        insertSta.setObject(j++, toDbType(e.getValue()));
                    }
                } else {
                    insertSta.setObject(2, toDbType(entry));
                }
                insertSta.addBatch();
            }
            insertSta.executeBatch();
        }
    }

    private void updateUserList(Connection conn, String tableName, String keyColumn, int keyId, String usersColumn, Collection<UUID> users) throws SQLException {
        String deleteSql;
        if (users.isEmpty()) {
            deleteSql = "DELETE FROM " + tablePrefix + tableName + " " +
                    " WHERE " + keyColumn + " = ?;";
        } else {
            deleteSql = "DELETE t FROM " + tablePrefix + tableName + " AS t" +
                    " WHERE t." + keyColumn + " = ?" +
                    " AND t." + usersColumn + " NOT IN " +
                    "(SELECT r.id FROM " + tablePrefix + "residents AS r WHERE r.uuid IN (" + String.join(",", Collections.nCopies(users.size(), "?")) + "));";
        }

        PreparedStatement deleteSta = conn.prepareStatement(deleteSql);
        deleteSta.setInt(1, keyId);
        int i = 2;
        for (UUID id : users) {
            deleteSta.setString(i++, id.toString());
        }
        deleteSta.executeUpdate();

        if (!users.isEmpty()) {
            String insertSql = getInsertIgnore() + " INTO " + tablePrefix + tableName + " (" + keyColumn + ", " + usersColumn + ")" +
                    " VALUES (?,(SELECT r.id FROM " + tablePrefix + "residents AS r WHERE r.uuid=?));";
            PreparedStatement insertSta = conn.prepareStatement(insertSql);
            for (UUID id : users) {
                insertSta.setInt(1, keyId);
                insertSta.setString(2, id.toString());
                insertSta.addBatch();
            }
            insertSta.executeBatch();
        }
    }

    // ------ Utility methods ----- //

    protected Map<String, Object> getValues(Connection conn, Map<String, ?> serialized, String... ignoredKeys) {
        Set<String> ignored = new HashSet<>(Arrays.asList(ignoredKeys));
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : serialized.entrySet()) {
            if (!ignored.contains(entry.getKey())) {
                if (entry.getValue() instanceof Collection || entry.getValue() instanceof Map) {
                    // ignored
                } else if (entry.getValue() instanceof ConfigurationSerializable) {
                    for (Map.Entry<String, Object> serEntry : getValues(conn, ((ConfigurationSerializable) entry.getValue()).serialize(), ignoredKeys).entrySet()) {
                        if (!entry.getKey().isEmpty()) {
                            values.put(entry.getKey() + "_" + serEntry.getKey(), serEntry.getValue());
                        } else {
                            values.put(serEntry.getKey(), serEntry.getValue());
                        }
                    }
                } else if (entry.getValue() instanceof UUID) {
                    Object id = null;
                    if (!entry.getValue().equals(Utils.ZERO_UUID)) {
                        Resident resident = ResidentManager.getInstance().getResident((UUID) entry.getValue());
                        if (resident != null) {
                            if (resident.getId() == null || resident.getId().isNull()) {
                                saveResident(conn, resident);
                            }
                            id = resident.getId().value();
                        }
                    }
                    if (!entry.getKey().endsWith("Id")) {
                        values.put(entry.getKey() + "Id", id);
                    } else {
                        values.put(entry.getKey(), id);
                    }
                } else {
                    values.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return values;
    }

    private Map<String, Object> toMap(ResultSet rs) throws SQLException {
        Map<String, Object> map = new LinkedHashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            map.put(metaData.getColumnLabel(i), rs.getObject(i));
        }
        return cascadeMap(map);
    }

    private Map<String, Object> cascadeMap(Map<String, Object> map) {
        Map<String, Map<String, Object>> specials = new LinkedHashMap<>();
        for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> e = it.next();
            String[] parts = e.getKey().split("_", 2);
            if (parts.length > 1) {
                it.remove();
                Map<String, Object> cascaded = specials.computeIfAbsent(parts[0], s -> new LinkedHashMap<>());
                cascaded.put(parts[1], e.getValue());
            }
        }
        for (Map<String, Object> special : specials.values()) {
            cascadeMap(special);
        }
        map.putAll(specials);
        return map;
    }

    protected Object toDbType(Object value) {
        if (value instanceof DatabaseSerializable) {
            throw new IllegalArgumentException(value + " is DatabaseSerializable and should be serialized properly!");
        }
        if (value instanceof IdObject) {
            value = ((IdObject) value).value();
        }
        if (value instanceof UUID) {
            value = value.toString();
        }
        if (value instanceof Enum) {
            value = ((Enum<?>) value).name();
        }
        return value;
    }

    // ---- Setup helpers --- ///

    public static class SaveQueue<T> {
        private SqlDatabaseManager db;
        private final String type;
        private BiConsumer<Connection, T> handleSave;

        private ScheduledTask task = null;
        private final Map<IdObject, T> queue = new ConcurrentHashMap<>();
        private final Runnable saving = () -> {
            Utils.catchSync();
            synchronized (queue) {
                forceSave = false;
                if (task != null) {
                    task.cancel();
                    task = null;
                }
                if (!queue.isEmpty()) {
                    String name = null;
                    Deque<Map.Entry<IdObject, T>> tQueue = new ArrayDeque<>(queue.entrySet());
                    Connection conn = null;
                    try {
                        while (!tQueue.isEmpty()) {
                            if (conn == null || !conn.isValid(10)) {
                                db.close(conn);
                                conn = db.getConnection();
                            }
                            Map.Entry<IdObject, T> e = tQueue.pollFirst();
                            if (e.getValue() != null) {
                                handleSave.accept(conn, e.getValue());
                                if (name == null) {
                                    name = e.getValue().getClass().getSimpleName();
                                }
                            }
                            queue.remove(e.getKey());
                        }
                    } catch (SQLException e) {
                        SettlementsPlugin.getInstance().getLogging().error("Error while saving " + queue.size() + " " + (name != null ? name : "object") + "s to database!");
                        SettlementsPlugin.getInstance().getLogging().exception(e);
                    } finally {
                        db.close(conn);
                    }
                }
            }
        };
        private final int maxSize;
        private final int saveWait;
        private boolean forceSave = false;

        public SaveQueue(String type, SqlDatabaseManager db, int maxSize, int saveWait, BiConsumer<Connection, T> handleSave) {
            this.type = type;
            this.db = db;
            this.maxSize = maxSize;
            this.saveWait = saveWait;
            this.handleSave = handleSave;
        }

        public void add(T object) {
            add(object instanceof DatabaseSerializable ? ((DatabaseSerializable) object).getId() : null, object);
        }

        public void add(IdObject id, T object) {
            if (id == null || id.isNull()) {
                id = IdObject.get(object.hashCode());
                forceSave = true;
            }
            queue.put(id, object);
            if (SettlementsPlugin.getInstance().isRunning() && queue.size() >= maxSize) {
                SettlementsPlugin.logDebug("Triggering " + type + " save queue due to add reaching max size (" + queue.size() + ")");
                Utils.runAsync(saving);
            } else if (!forceSave && task == null && SettlementsPlugin.getInstance().isEnabled()) {
                task = Bukkit.getAsyncScheduler().runDelayed(
                    SettlementsPlugin.getInstance(),
                    _ -> saving.run(),
                    saveWait,
                    TimeUnit.SECONDS
                );
            }
        }

        /**
         * Finish up adding to this queue and check if we should save or not
         * @return Whether or not a save was run due to finishing up
         */
        public boolean finishAdding() {
             if (forceSave || (SettlementsPlugin.getInstance().isRunning() && queue.size()  > maxSize / 2)) {
                 if (queue.size()  > maxSize / 2) {
                     SettlementsPlugin.logDebug("Triggering " + type + " save queue due to finnish reaching half size (" + queue.size() + ")");
                 }
                 if (task != null) {
                     task.cancel();
                 }
                 Utils.runAsync(saving);
                 return true;
             }
             return false;
        }

        public boolean remove(T object) {
            return remove(object instanceof DatabaseSerializable ? ((DatabaseSerializable) object).getId() : null, object);
        }

        public boolean remove(IdObject id, T object) {
            boolean r;
            if (id == null || id.isNull()) {
                r = queue.values().remove(object);
            } else {
                r = queue.remove(id, object);
            }

            if (r && task != null && queue.isEmpty()) {
                task.cancel();
            }
            return r;
        }

        /**
         * Force a save of this queue
         */
        public void forceSave() {
            forceSave = true;
            finishAdding();
        }
    }
}
