package me.crz.minecraft.kingdoms.db;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.chat.ChatColorType;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.lock.Lock;
import me.crz.minecraft.kingdoms.missions.MissionData;
import me.crz.minecraft.kingdoms.portal.Portal;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class SqlDatabaseManager extends DatabaseManager {
    protected String tablePrefix;
    protected final KingdomsPlugin plugin = KingdomsPlugin.getInstance();

    private SaveQueue<Village> villageQueue;
    private SaveQueue<Empire> empireQueue;
    private SaveQueue<Resident> residentQueue;
    private SaveQueue<OwnedLand> landQueue;
    private SaveQueue<Portal> portalQueue;
    private SaveQueue<Consumer<Connection>> lockQueue;

    @Override
    public void connect() throws Exception {
        tablePrefix = Config.Storage.TABLE_PREFIX;

        int queueSize = Config.Storage.Queue.SIZE;
        int queueWait = Config.Storage.Queue.WAIT;

        villageQueue = new SaveQueue<>(this, queueSize, queueWait, this::saveVillage);
        empireQueue = new SaveQueue<>(this, queueSize, queueWait, this::saveEmpire);
        residentQueue = new SaveQueue<>(this, queueSize, queueWait, this::saveResident);
        landQueue = new SaveQueue<>(this, queueSize, queueWait, this::saveLand);
        portalQueue = new SaveQueue<>(this, queueSize, queueWait, this::savePortal);
        lockQueue = new SaveQueue<>(this, queueSize, queueWait, (conn, consumer) -> consumer.accept(conn));

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
    public void addNewEmpire(Empire empire) {
        Utils.catchSync();
        saveEmpire(getConnection(), empire);
    }

    @Override
    public void saveEmpire(Empire empire) {
        empireQueue.add(empire.getId(), empire);
        empireQueue.finishAdding();
    }

    @Override
    public void saveEmpires(Collection<Empire> empires) {
        for (Empire empire : empires) {
            empireQueue.add(empire.getId(), empire);
        }
        empireQueue.finishAdding();
    }

    private void saveEmpire(Connection conn, Empire empire) {
        Utils.catchSync();
        try {
            if (!save(conn, empire, "members", "permissions").isNull() && !empire.getId().isNull()) {
                updateDataMap(conn, "empire_vaults", "empireId", (Integer) empire.getId().value(), "content", "amount", empire.getVault());
                updateDataMap(conn, "empire_uses", "empireId", (Integer) empire.getId().value(), "type", "state",
                        empire.getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                updateDataMap(conn, "empire_perks", "empireId", (Integer) empire.getId().value(), "perk", "",
                        empire.getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
                updateDataMap(conn, "empire_permissions", "empireId", (Integer) empire.getId().value(), "rank", "permission", empire.getRanksPermissions(), "value");
                updateDataMap(conn, "empire_titles", "empireId", (Integer) empire.getId().value(), "rank", "title", empire.getRankTitles());
                updateDataMap(conn, "empire_defaulttitles", "empireId", (Integer) empire.getId().value(), "rank", "title", empire.getDefaultRankTitles());
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving empire " + empire.getName() + " to database!", e);
        }
    }

    @Override
    public void deleteEmpire(Empire empire) {
        Utils.catchSync();
        empireQueue.remove(empire.getId(), empire);
        Connection conn = getConnection();
        if (!empire.getId().isNull()) {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "empires WHERE id=?")) {
                sta.setInt(1, (Integer) empire.getId().value());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete empire " + empire.getId().value() + "/" + empire.getName() + " from database!", e);
            } finally {
                close(conn);
            }
        } else {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "empires WHERE name = ? LIMIT 1")) {
                sta.setString(1, empire.getName());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete empire " + empire.getId().value() + "/" + empire.getName() + " from database!", e);
            } finally {
                close(conn);
            }
        }
    }

    @Override
    public void addNewVillage(Village village) {
        Utils.catchSync();
        saveVillage(getConnection(), village);
    }

    @Override
    public void saveVillage(Village village) {
        villageQueue.add(village.getId(), village);
        villageQueue.finishAdding();
    }

    private void saveVillage(Connection conn, Village village) {
        try {
            if (!save(conn, village, "residents", "permissions", "missiondata").isNull() && !village.getId().isNull()) {
                updateDataMap(conn, "settlement_vaults", "settlementId", (Integer) village.getId().value(), "content", "amount", village.getVault());
                updateDataMap(conn, "settlement_outposts", "settlementId", (Integer) village.getId().value(), "name", "", village.getOutpostHomes());
                updateDataMap(conn, "settlement_uses", "settlementId", (Integer) village.getId().value(), "type", "state",
                        village.getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                updateDataMap(conn, "settlement_perks", "settlementId", (Integer) village.getId().value(), "perk", "",
                        village.getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
                updateDataMap(conn, "settlement_permissions", "settlementId", (Integer) village.getId().value(), "rank", "permission", village.getRanksPermissions(), "value");
                updateDataMap(conn, "settlement_titles", "settlementId", (Integer) village.getId().value(), "rank", "title", village.getRankTitles());
                updateDataMap(conn, "settlement_defaulttitles", "settlementId", (Integer) village.getId().value(), "rank", "title", village.getDefaultRankTitles());
                updateDataMap(conn, "settlement_missiondata", "settlementId", (Integer) village.getId().value(), "mission", "type", village.getMissionDataMap(), "progress");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving village " + village.getName() + " to database!", e);
        }
    }

    @Override
    public void saveVillages(Collection<Village> villages) {
        for (Village village : villages) {
            villageQueue.add(village.getId(), village);
        }
        villageQueue.finishAdding();
    }

    @Override
    public void deleteVillage(Village village) {
        Utils.catchSync();
        villageQueue.remove(village.getId(), village);
        Connection conn = getConnection();
        if (!village.getId().isNull()) {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "settlements WHERE id=?")) {
                sta.setInt(1, (Integer) village.getId().value());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + village.getId().value() + "/" + village.getName() + " from database!", e);
            } finally {
                close(conn);
            }
        } else {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "settlements WHERE name=?")) {
                sta.setString(1, village.getName());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + village.getId().value() + "/" + village.getName() + " from database!", e);
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
    public void unclaimLand(Village village) {
        Utils.catchSync();
        if (village.getId().value() == null) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims of settlement " + village.getName() + " from database! The village has no ID?");
            return;
        }

        for (OwnedLand land : LandManager.getInstance().getClaimedLand(village)) {
            landQueue.remove(land.getId(), land);
        }

        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE settlementId = ?;")) {
            sta.setInt(1, (Integer) village.getId().value());
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims of settlement " + village.getName() + " from database! ", e);
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
        return Collections.EMPTY_LIST;
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
    public Collection<Village> getAllVillages() {
        Utils.catchSync();
        String sql = "SELECT s.*, r.uuid AS chief FROM " + tablePrefix + "settlements AS s LEFT JOIN " + tablePrefix + "residents AS r ON s.chiefId = r.id;";
        Connection conn = getConnection();
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            List<Village> villages = new ArrayList<>();
            while (rs.next()) {
                Village village = createVillage(conn, rs);
                if (village != null) {
                    villages.add(village);
                }
            }
            return villages;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all settlements from database!", e);
        } finally {
            close(conn);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<Empire> getAllEmpires() {
        Utils.catchSync();
        String sql = "SELECT * FROM " + tablePrefix + "empires;";
        Connection conn = getConnection();
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            List<Empire> empires = new ArrayList<>();
            while (rs.next()) {
                Empire empire = createEmpire(conn, rs);
                if (empire != null) {
                    empires.add(empire);
                }
            }
            return empires;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all empires from database!", e);
        } finally {
            close(conn);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Resident getResident(UUID uuid) {
        Utils.catchSync();
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
        Utils.catchSync();
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
    public void saveResident(Resident... residents) {
        for (Resident resident : residents) {
            residentQueue.add(IdObject.get(resident.getId()), resident);
        }
        residentQueue.finishAdding();
    }

    private void saveResident(Connection conn, Resident resident) {
        if (resident == null) {
            return;
        }
        Utils.catchSync();
        Map<String, Object> values = getValues(resident.serialize());
        try {
            PreparedStatement sta = executeSerializableInsert(conn, "residents", resident, values);
            if (resident.getId().isNull()) {
                ResultSet rs = sta.getGeneratedKeys();
                if (rs.next()) {
                    resident.setId(IdObject.get(rs.getInt(1)));
                }
            }
            if (!resident.getId().isNull()) {
                updateList(conn, "resident_channels", "userId", (Integer) resident.getId().value(), "channel", resident.getChannels().stream().map(ChatChannel::name).collect(Collectors.toList()));
                updateUserList(conn, "resident_friends", "userId", (Integer) resident.getId().value(), "friendId", resident.getFriends());
                updateUserList(conn, "resident_ignores", "userId", (Integer) resident.getId().value(), "ignoredId", resident.getIgnore());
                updateDataMap(conn, "resident_channel_colors", "userId", (Integer) resident.getId().value(), "channel", "color", Arrays.stream(ChatChannel.values())
                        .filter(c -> resident.getChannelColor(c, ChatColorType.MESSAGE) != null)
                        .collect(Collectors.toMap(Enum::name, c -> resident.getChannelColor(c, ChatColorType.MESSAGE).name())));
                updateDataMap(conn, "resident_channel_prefix_colors", "userId", (Integer) resident.getId().value(), "channel", "color", Arrays.stream(ChatChannel.values())
                        .filter(c -> resident.getChannelColor(c, ChatColorType.PREFIX) != null)
                        .collect(Collectors.toMap(Enum::name, c -> resident.getChannelColor(c, ChatColorType.PREFIX).name())));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving player " + resident.getName() + "/" + resident.getUUID() + " to database!", e);
        }
    }

    @Override
    public void savePortals(Portal... portals) {
        for (Portal portal : portals) {
            portalQueue.add(portal.getId(), portal);
        }
        portalQueue.finishAdding();
    }

    @Override
    public void savePortal(Portal portal) {
        Utils.catchSync();
        portalQueue.add(portal.getId(), portal);
        portalQueue.finishAdding();
    }

    private void savePortal(Connection conn, Portal portal) {
        try {
            if (!save(conn, portal, "links").isNull() && !portal.getId().isNull()) {
                updateList(conn, "portal_links", "sourceId", (Integer) portal.getId().value(), "targetId", portal.getLinks());
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving portal at " + portal.getLocation() + " to database!", e);
        }
    }

    @Override
    public Collection<Portal> getAllPortals() {
        Utils.catchSync();
        String sql = "SELECT * FROM " + tablePrefix + "portals;";
        Connection conn = getConnection();
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            List<Portal> portals = new ArrayList<>();
            while (rs.next()) {
                Portal portal = createPortal(conn, rs);
                if (portal != null) {
                    portals.add(portal);
                }
            }
            return portals;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all settlements from database!", e);
        } finally {
            close(conn);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void deletePortals(Portal... portals) {
        Utils.catchSync();
        List<Portal> withId = new ArrayList<>();
        List<Portal> withoutId = new ArrayList<>();

        for (Portal portal : portals) {
            portalQueue.remove(portal.getId(), portal);
            if (portal.getId().isNull()) {
                withoutId.add(portal);
            } else {
                withId.add(portal);
            }
        }

        if (!withId.isEmpty()) {
            Connection conn = getConnection();
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "portals WHERE id=?")) {
                for (Portal portal : withId) {
                    sta.setInt(1, (Integer) portal.getId().value());
                    sta.addBatch();
                }
                sta.executeBatch();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + withId.size() + " portals from database!", e);
            } finally {
                close(conn);
            }
        }

        if (!withoutId.isEmpty()) {
            Connection conn = getConnection();
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "portals WHERE world = ? AND x = ? AND y = ? AND z = ? LIMIT 1")) {
                for (Portal portal : withoutId) {
                    sta.setString(1, portal.getLocation().getWorld());
                    sta.setInt(2, portal.getLocation().getX());
                    sta.setInt(3, portal.getLocation().getY());
                    sta.setInt(4, portal.getLocation().getZ());
                    sta.addBatch();
                }
                sta.executeBatch();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + withoutId.size() + " portals from database!", e);
            } finally {
                close(conn);
            }
        }
    }


    @Override
    public void renameLockWorld(String oldName, String newName) {
        Utils.catchSync();

        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement("UPDATE " + tablePrefix + "locks SET world = ? WHERE world = ?;")) {
            sta.setString(1, newName);
            sta.setString(2, oldName);
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to change locks world from " + oldName + " to " + newName + " in database! ", e);
        } finally {
            close(conn);
        }
    }

    @Override
    public void saveLocks(Lock... locks) {
        for (Lock lock : locks) {
            if (lock.isValid()) {
                lockQueue.add(lock.getId(), conn -> {
                    try {
                        save(conn, lock);
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, "Error while saving lock at " + lock.getLocation().getKey() + " to database!", e);
                    }
                });
            }
        }
        lockQueue.finishAdding();
    }

    @Override
    public void deleteLocks(Lock... locks) {
        if (locks.length == 0) {
            return;
        }
        for (Lock lock : locks) {
            lockQueue.add(lock.getId(), conn -> {
                try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "locks WHERE " +
                         (lock.getId().isNull() ? "world = ? AND x = ? AND y = ? AND z = ?;" : "id = ?;"))) {
                    if (lock.getId().isNull()) {
                        sta.setString(1, lock.getLocation().getWorld());
                        sta.setInt(2, lock.getLocation().getX());
                        sta.setInt(3, lock.getLocation().getY());
                        sta.setInt(4, lock.getLocation().getZ());
                    } else {
                        sta.setObject(1, lock.getId().value());
                    }
                    sta.execute();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + locks.length + " locks from database!", e);
                }
            });
        }
        lockQueue.finishAdding();
    }

    @Override
    public void deleteLocksInChunks(Collection<? extends SimpleChunkLocation> chunks) {
        if (chunks.isEmpty()) {
            return;
        }

        lockQueue.add(conn -> {
            String sql = "DELETE FROM " + tablePrefix + "locks" +
                    " WHERE world = ? AND x BETWEEN ? AND ? AND z BETWEEN ? AND ?;";
            try (PreparedStatement sta = conn.prepareStatement(sql)) {
                for (SimpleChunkLocation chunk : chunks) {
                    sta.setString(1, chunk.getWorld());
                    sta.setInt(2, chunk.getX() << 4);
                    sta.setInt(3, (chunk.getX() << 4) + 16);
                    sta.setInt(4, chunk.getZ() << 4);
                    sta.setInt(5, (chunk.getZ() << 4) + 16);
                    sta.addBatch();
                }
                sta.executeBatch();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete locks in " + chunks.size() + " chunks from database!", e);
            }
        });
        lockQueue.forceSave = true;
        lockQueue.finishAdding();
    }

    @Override
    public void deleteLocks(String worldName) {
        lockQueue.add(conn -> {
            try (PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "locks WHERE world = ?;")) {
                sta.setString(1, worldName);
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete locks in world " + worldName + " from database!", e);
            }
        });
        lockQueue.forceSave = true;
        lockQueue.finishAdding();
    }

    @Override
    public Collection<Lock> getAllLocks() {
        Utils.catchSync();
        // make sure to save before getting new locks
        lockQueue.forceSave();
        String sql = "SELECT l.*, r.uuid AS `owner` FROM " + tablePrefix + "locks AS l LEFT JOIN " + tablePrefix + "residents AS r ON l.ownerId = r.id;";
        Connection conn = getConnection();
        try (ResultSet rs = conn.createStatement().executeQuery(sql)) {
            List<Lock> locks = new ArrayList<>();
            while (rs.next()) {
                Lock lock = createLock(conn, rs);
                if (lock != null) {
                    locks.add(lock);
                }
            }
            return locks;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all locks from database!", e);
        } finally {
            close(conn);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<Lock> getLocks(SimpleChunkLocation... chunks) {
        if (chunks.length == 0) {
            return  Collections.EMPTY_LIST;
        }
        Utils.catchSync();
        // make sure to save before getting new locks
        lockQueue.forceSave();
        String sql = "SELECT l.*, r.uuid AS `owner` FROM " + tablePrefix + "locks AS l" +
                " LEFT JOIN " + tablePrefix + "residents AS r ON l.ownerId = r.id" +
                " WHERE world = ? AND x BETWEEN ? AND ? AND z BETWEEN ? AND ?;";
        List<Lock> locks = new ArrayList<>();
        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement(sql)) {
            for (SimpleChunkLocation chunk : chunks) {
                sta.setString(1, chunk.getWorld());
                sta.setInt(2, chunk.getX() << 4);
                sta.setInt(3, (chunk.getX() << 4) + 16);
                sta.setInt(4, chunk.getZ() << 4);
                sta.setInt(5, (chunk.getZ() << 4) + 16);
                ResultSet rs = sta.executeQuery();
                while (rs.next()) {
                    Lock lock = createLock(conn, rs);
                    if (lock != null) {
                        locks.add(lock);
                    }
                }
            }
            return locks;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get locks in " + chunks.length + " chunks from database!", e);
        } finally {
            close(conn);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void log(Empire empire, Village village, Resident resident, String action, Map<String, Long> values) {
        if (values.isEmpty()
                || (village != null && village.getId().isNull())
                || (empire != null && empire.getId().isNull())) {
            return;
        }
        Utils.catchSync();
        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement("INSERT INTO " + tablePrefix + "logs " +
                     "(`empireId`, `settlementId`, `residentId`, `action`, `type`, `amount`) " +
                     "VALUES (?, ?, ?, ?, ?, ?);"
             )
        ) {
            for (Map.Entry<String, Long> value : values.entrySet()) {
                if (value.getValue() != 0) {
                    sta.setInt(1, empire != null ? (Integer) empire.getId().value() : -1);
                    sta.setInt(2, village != null ? (Integer) village.getId().value() : -1);
                    sta.setInt(3, resident != null && resident.getId().isNull() ? (Integer) resident.getId().value() : -1);
                    sta.setString(4, action.toLowerCase());
                    sta.setString(5, value.getKey().toLowerCase());
                    sta.setLong(6, value.getValue());
                    sta.addBatch();
                }
            }
            sta.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn);
        }
    }

    @Override
    public int getLogPageCount(Village village, Resident resident, LogType type) {
        return getLogPageCount("settlementId", village.getId(), resident, type);
    }

    @Override
    public int getLogPageCount(Empire empire, Resident resident, LogType type) {
        return getLogPageCount("empireId", empire.getId(), resident, type);
    }

    private int getLogPageCount(String column, IdObject id, Resident resident, LogType type) {
        if (id.isNull()) {
            return 0;
        }
        String sql = "SELECT count(l.id) " +
                "FROM " + tablePrefix + "logs AS l " +
                "WHERE l." + column + "=?";
        switch (type) {
            case BANK:
                sql += " AND l.type = 'cash'";
                break;
            case VAULT:
                sql += " AND l.type != 'cash' AND l.type != 'claim'";
                break;
        }
        if (!resident.getVillageRank().atLeast(VillageRank.getChief())) {
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
    public List<String[]> getLog(Village village, Resident resident, int page, LogType type) {
        return getLog("settlementId", village.getId(), resident, page, type);
    }

    @Override
    public List<String[]> getLog(Empire empire, Resident resident, int page, LogType type) {
        return getLog("empireId", empire.getId(), resident, page, type);
    }

    @Override
    public void disable() {
        villageQueue.forceSave();
        empireQueue.forceSave();
        residentQueue.forceSave();
        landQueue.forceSave();
        lockQueue.forceSave();
        try {
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<String[]> getLog(String column, IdObject id, Resident resident, int page, LogType type) {
        List<String[]> log = new ArrayList<>();
        if (id.isNull() || page < 1) {
            return log;
        }
        int start = (page-1) * Config.ENTRIES_PER_PAGE;
        String sql = "SELECT l.empireId, l.settlementId, l.action, l.type, l.amount, l.timestamp, r.name AS playerName, r.uuid AS playerId, r.rank AS playerRank, r.title AS playerTitle " +
                "FROM " + tablePrefix + "logs AS l " +
                "LEFT JOIN " + tablePrefix + "residents AS r ON l.residentId = r.id " +
                "WHERE l." + column + "=?";
        switch (type) {
            case BANK:
                sql += " AND l.type = 'cash'";
                break;
            case VAULT:
                sql += " AND l.type != 'cash' AND l.type != 'claim'";
                break;
        }
        if (!resident.getVillageRank().atLeast(VillageRank.getChief())) {
            sql += " AND (l.action = 'deposit' OR l.action = 'donate')";
        }
        sql += " ORDER BY l.id DESC LIMIT " + start + "," + Config.ENTRIES_PER_PAGE + ";";
        Connection conn = getConnection();
        try (PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setInt(1, (Integer) id.value());
            ResultSet rs = sta.executeQuery();
            while (rs.next()) {
                int amount = rs.getInt("amount");
                if (amount == 0) {
                    continue;
                }
                boolean hasPlayer = rs.getString("playerId") != null;
                VillageRank rank = VillageRank.getDefault();
                if (hasPlayer) {
                    try {
                        rank = VillageRank.valueOf(rs.getString("playerRank").toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }

                Village village = null;
                int villageId = rs.getInt("settlementId");
                if (villageId > -1) {
                    village = VillageManager.getInstance().getVillageFromId(IdObject.get(villageId));
                }
                Empire empire = null;
                int empireId = rs.getInt("empireId");
                if (empireId > -1) {
                    empire = EmpireManager.getInstance().getEmpireFromId(IdObject.get(empireId));
                }

                log.add(new String[] {
                        "village", village != null ? village.getName() : villageId > -1 ? "id:" + villageId : "",
                        "villagestate", village != null ? village.getState().getTag() : villageId > -1 ? "Unknown" : "",
                        "empire", empire != null ? empire.getName() : empireId > -1 ? "id:" + empireId : "",
                        "name", hasPlayer ? rs.getString("playerName") : "",
                        "uuid", hasPlayer ? rs.getString("playerId") : "",
                        "rank", hasPlayer ? rank.getTag(village) : "",
                        "title", hasPlayer ? rs.getString("playerTitle") : "",
                        "action", rs.getString("action") != null ? rs.getString("action") : "",
                        "type", rs.getString("type") != null ? rs.getString("type") : "?",
                        "icon", Utils.getIcon(resident, rs.getString("type")),
                        "amount", String.valueOf(amount),
                        "timestamp", Utils.TIME_FORMAT.format(new Date(rs.getTimestamp("timestamp").getTime()))
                });
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get log for " + id, e);
        } finally {
            close(conn);
        }
        return log;
    }

    // ------ Creation methods ----- //

    private Empire createEmpire(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Map<String, Object> map = toMap(rs);

        PreparedStatement rSta = conn.prepareStatement("SELECT id FROM " + tablePrefix + "settlements WHERE empireId = ?;");
        rSta.setInt(1, id);
        ResultSet rRs = rSta.executeQuery();
        Collection<Integer> members = new ArrayList<>();
        while (rRs.next()) {
            members.add(rRs.getInt("id"));
        }
        map.put("members", members);

        map.put("banished", selectList(conn, "empire", id, "banished"));
        map.put("allies", selectList(conn, "empire", id, "allies"));

        map.put("uses", selectMap(conn, "empire_uses", "empireId", id, "type", "state", String.class));
        map.put("vault", selectMap(conn, "empire_vaults", "empireId", id, "content", "amount", Long.class));
        map.put("defaulttitles", selectMap(conn, "empire_defaulttitles", "empireId", id, "rank", "title", String.class));

        PreparedStatement pSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "empire_perks WHERE empireId = ?;");
        pSta.setInt(1, id);
        ResultSet pRs = pSta.executeQuery();
        Map<String, Map<String, Object>> unlockedPerks = new LinkedHashMap<>();
        while (pRs.next()) {
            unlockedPerks.put(pRs.getString("perk").toLowerCase(), toMap(pRs));
        }
        map.put("perks", unlockedPerks);

        PreparedStatement rpSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "empire_permissions WHERE empireId = ?;");
        rpSta.setInt(1, id);
        ResultSet rpRs = rpSta.executeQuery();
        Map<String, Map<String, Boolean>> rankPermissions = new LinkedHashMap<>();
        while (rpRs.next()) {
            rankPermissions.computeIfAbsent(rpRs.getString("rank").toLowerCase(), k -> new LinkedHashMap<>())
                    .put(rpRs.getString("permission"), rpRs.getBoolean("value"));
        }
        map.put("permissions", rankPermissions);

        PreparedStatement tpSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "empire_titles WHERE empireId = ?;");
        tpSta.setInt(1, id);
        ResultSet tpRs = tpSta.executeQuery();
        Map<String, Collection<String>> rankTitles = new LinkedHashMap<>();
        while (tpRs.next()) {
            rankTitles.computeIfAbsent(tpRs.getString("rank").toLowerCase(), k -> new LinkedHashSet<>()).add(tpRs.getString("title"));
        }
        map.put("titles", rankTitles);

        return Empire.deserialize(map);
    }

    private Village createVillage(Connection conn, ResultSet rs) throws SQLException {
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

        map.put("banished", selectList(conn, "settlement", id, "banished"));
        map.put("allies", selectList(conn, "settlement", id, "allies"));

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


        return Village.deserialize(map);
    }

    private Resident createResident(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        int id = rs.getInt("id");
        map.put("channels", selectList(conn, "resident_channels", "userId", id, "channel"));
        map.put("friends", selectResidentList(conn, "resident_friends", "userId", id, "friendId"));
        map.put("ignores", selectResidentList(conn, "resident_ignores", "userId", id, "ignoredId"));
        map.put("channelColors", selectMap(conn, "resident_channel_colors", "userId", id, "channel", "color", String.class));
        map.put("channelPrefixColors", selectMap(conn, "resident_channel_prefix_colors", "userId", id, "channel", "color", String.class));
        return Resident.deserialize(map);
    }

    private OwnedLand createLand(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        int id = rs.getInt("id");
        map.put("modes", selectMap(conn, "claim_modes", "claimId", id, "mode", "enabled", Boolean.class));
        map.put("uses", selectMap(conn, "claim_uses", "claimId", id, "type", "state", String.class));
        map.put("plotMembers", selectResidentList(conn, "claim_members", "claimId", id, "memberId"));
        return OwnedLand.deserialize(map);
    }

    private Portal createPortal(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        map.put("links", selectList(conn, "portal_links", "sourceId", rs.getInt("id"), "targetId"));
        return Portal.deserialize(map);
    }

    private Lock createLock(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        map.put("allowed", selectList(conn, "lock_allowed", "lockId", rs.getInt("id"), "allowed"));
        return Lock.deserialize(map);
    }

    // ------ Database operation helpers ----- //

    private IdObject save(Connection conn, DatabaseSerializable serializable, String... ignored) throws SQLException {
        if (serializable == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Tried to save something to the database but it was null?", new NullPointerException().fillInStackTrace());
            return IdObject.get(null);
        }
        Map<String, Object> serialized = serializable.serialize();
        Map<String, Object> values = getValues(serialized, ignored);
        PreparedStatement sta = executeSerializableInsert(conn, serializable.getDbName() + "s", serializable, values);
        if (serializable.getId().isNull() ) {
            ResultSet rs = sta.getGeneratedKeys();
            if (rs.next()) {
                serializable.setId(IdObject.get(rs.getInt(1)));
            }
        }
        if (!serializable.getId().isNull()) {
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
            deleteSta.setObject(entry.getKey(), entry.getValue());
        }
        deleteSta.executeUpdate();

        if (!values.isEmpty()) {
            Object value = values.values().iterator().next();
            Set<String> columns = new LinkedHashSet<>();
            if (value instanceof ConfigurationSerializable) {
                value = ((ConfigurationSerializable) value).serialize();
            }
            if (value instanceof Map && mapValueColumns.length == 0) {
                if (((Map) value).keySet().iterator().next() instanceof String) {
                    for (String column : getValues((Map<String, ?>) value, mapKey).keySet()) {
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

    private void updateList(Connection conn, String tableName, String keyColumn, int keyId, String entryColumn, Collection<?> entries) throws SQLException {
        String deleteSql;
        if (entries.isEmpty()) {
            deleteSql = "DELETE FROM " + tablePrefix + tableName + " " +
                    " WHERE " + keyColumn + " = ?;";
        } else {
            deleteSql = "DELETE FROM " + tablePrefix + tableName + " " +
                    " WHERE " + keyColumn + " = ?" +
                    " AND " + entryColumn + " NOT IN (" + String.join(",", Collections.nCopies(entries.size(), "?")) + ");";
        }

        PreparedStatement deleteSta = conn.prepareStatement(deleteSql);
        deleteSta.setInt(1, keyId);
        int i = 2;
        for (Object entry : entries) {
            if (entry instanceof IdObject) {
                deleteSta.setInt(i++, (int) ((IdObject) entry).value());
            } else {
                deleteSta.setObject(i++, entry);
            }
        }
        deleteSta.executeUpdate();

        if (!entries.isEmpty()) {
            String insertSql = getInsertIgnore() + " INTO " + tablePrefix + tableName + " (" + keyColumn + ", " + entryColumn + ")" +
                    " VALUES (?,?);";
            PreparedStatement insertSta = conn.prepareStatement(insertSql);
            for (Object entry : entries) {
                insertSta.setInt(1, keyId);
                if (entry instanceof IdObject) {
                    insertSta.setInt(2, (int) ((IdObject) entry).value());
                } else {
                    insertSta.setObject(2, entry);
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

    protected Map<String, Object> getValues(Map<String, ?> serialized, String... ignoredKeys) {
        Set<String> ignored = new HashSet<>(Arrays.asList(ignoredKeys));
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : serialized.entrySet()) {
            if (!ignored.contains(entry.getKey())) {
                if (entry.getValue() instanceof Collection || entry.getValue() instanceof Map) {
                    // ignored
                } else if (entry.getValue() instanceof ConfigurationSerializable) {
                    for (Map.Entry<String, Object> serEntry : getValues(((ConfigurationSerializable) entry.getValue()).serialize(), ignoredKeys).entrySet()) {
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
                        id = resident != null ? resident.getId().value() : null;
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

    // ---- Setup helpers --- ///

    public static class SaveQueue<T> {
        private SqlDatabaseManager db;
        private BiConsumer<Connection, T> handleSave;

        private BukkitTask task = null;
        private final Map<IdObject, T> queue = new ConcurrentHashMap<>();
        private Runnable saving = () -> {
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
                            if (conn == null || !conn.isValid(1)) {
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
                        KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while saving " + queue.size() + " " + (name != null ? name : "object") + "s to database!", e);
                    } finally {
                        db.close(conn);
                    }
                }
            }
        };
        private int maxSize;
        private int saveWait;
        private boolean forceSave = false;

        public SaveQueue(SqlDatabaseManager db, int maxSize, int saveWait, BiConsumer<Connection, T> handleSave) {
            this.db = db;
            this.maxSize = maxSize;
            this.saveWait = saveWait;
            this.handleSave = handleSave;
        }

        public void add(T object) {
            add(null, object);
        }

        public void add(IdObject id, T object) {
            synchronized (queue) {
                if (id == null || id.isNull()) {
                    id = IdObject.get(object.hashCode());
                    forceSave = true;
                }
                queue.put(id, object);
                if (queue.size() >= maxSize) {
                    Utils.runAsync(saving);
                } else if (!forceSave && task == null) {
                    task = Bukkit.getScheduler().runTaskLaterAsynchronously(KingdomsPlugin.getInstance(), saving, saveWait * 20);
                }
            }
        }

        /**
         * Finish up adding to this queue and check if we should save or not
         * @return Whether or not a save was run due to finishing up
         */
        public boolean finishAdding() {
             if (forceSave || (KingdomsPlugin.getInstance().isRunning() && queue.size()  > maxSize / 2)) {
                 Utils.runAsync(saving);
                 return true;
             }
             return false;
        }

        public boolean remove(T object) {
            return remove(null, object);
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
