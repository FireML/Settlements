package me.crz.minecraft.kingdoms.db;

import com.zaxxer.hikari.HikariDataSource;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.lock.Lock;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Pair;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MySqlDatabaseManager extends DatabaseManager {
    private HikariDataSource ds;
    private String tablePrefix;
    private final KingdomsPlugin plugin = KingdomsPlugin.getInstance();

    private SaveQueue<Village> villageQueue;
    private SaveQueue<Empire> empireQueue;
    private SaveQueue<Resident> residentQueue;
    private SaveQueue<OwnedLand> landQueue;
    private SaveQueue<Lock> lockQueue;

    @Override
    public void connect() throws Exception {
        ConfigurationSection config = KingdomsPlugin.getInstance().getConfig().getConfigurationSection("storage");
        tablePrefix = config.getString("table-prefix");
        String host = config.getString("host");
        int port = config.getInt("port");
        String database = config.getString("database");
        String urlPar = config.getString("url-parameters");

        int queueSize = config.getInt("queue.size");
        int queueWait = config.getInt("queue.wait");

        ds = new HikariDataSource();
        initConnectionClass(ds);

        ds.addDataSourceProperty("url", "jdbc:mysql://" + host + ":" + port + "/" + database + urlPar);
        ds.setUsername(config.getString("user"));
        ds.setPassword(config.getString("password"));
        ds.setConnectionTimeout(5000);

        villageQueue = new SaveQueue<>(ds, queueSize, queueWait, this::saveVillage);
        empireQueue = new SaveQueue<>(ds, queueSize, queueWait, this::saveEmpire);
        residentQueue = new SaveQueue<>(ds, queueSize, queueWait, this::saveResident);
        landQueue = new SaveQueue<>(ds, queueSize, queueWait, this::saveLand);
        lockQueue = new SaveQueue<>(ds, queueSize, queueWait, (conn, lock) -> {
            try {
                save(conn, lock);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while saving lock at " + lock.getLocation().getKey() + " to database!", e);
            }
        });

        initializeTables();
        updateClaimModes();
        updateClaimModesUniqueKey();
        updateAllies();
        updateLandOwnersMissingCollumns();
    }

    private void initializeTables() throws SQLException {
        try (Connection conn = ds.getConnection(); Statement sta = conn.createStatement()) {
            sta.addBatch("SET FOREIGN_KEY_CHECKS = 0");

            for (MySqlTable table : MySqlTable.values()) {
                sta.addBatch(table.getSql(tablePrefix));
            }

            sta.addBatch("SET FOREIGN_KEY_CHECKS = 1");

            sta.executeBatch();
        }
    }

    private void updateClaimModes() {
        String sql = "SELECT * FROM " + tablePrefix + "claims;";
        try (Connection conn = ds.getConnection();
             Statement sta = conn.createStatement()) {
            boolean found = false;
            ResultSet rs = sta.executeQuery(sql);
            while (rs.next()) {
                found = true;
                try {
                    rs.getBoolean("pvp"); // test if column exists
                    updateDataMap(conn, "claim_modes", "claimId", rs.getInt("id"), "mode", "enabled", PlotMode.values(PlotMode.AppliesTo.LAND_OWNER).stream()
                            .map(m -> {
                                try {
                                    return new Pair<>(m.name(), rs.getBoolean(m.name()));
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            }).filter(Objects::nonNull).collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
                } catch (SQLException e) {
                    // column doesn't exist -> already updated. Don't do anything
                    return;
                }
            }
            if (found) {
                sta.execute("ALTER TABLE " + tablePrefix + "claims "
                        + PlotMode.values(PlotMode.AppliesTo.LAND_OWNER).stream().map(m -> "DROP COLUMN " + m.name()).collect(Collectors.joining(","))
                        +";");
            } else {
                sta.addBatch("SET FOREIGN_KEY_CHECKS = 0");
                sta.addBatch("DROP TABLE " + tablePrefix + "claims;");
                sta.addBatch(MySqlTable.CLAIMS.getSql(tablePrefix));
                sta.addBatch("SET FOREIGN_KEY_CHECKS = 1");
                sta.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying update claim's table to new mode format!", e);
        }
    }

    private void updateClaimModesUniqueKey() {
        try (Connection conn = ds.getConnection()) {
            Statement sta = conn.createStatement();
            // test if index exists:
            sta.execute("DROP INDEX mode ON " + tablePrefix + MySqlTable.CLAIM_MODES.getDbName());
            // if it exists remove the table and create new one
            sta.execute("DROP TABLE " + tablePrefix + MySqlTable.CLAIM_MODES.getDbName());
            sta.execute(MySqlTable.CLAIM_MODES.getSql(tablePrefix));
        } catch (SQLException e) {
            // index doesn't exist
        }
    }

    private void updateAllies() {
        alterColumnOrRecreate(MySqlTable.SETTLEMENT_ALLIES, "ally", "allies", "VARCHAR(320) NOT NULL");
        alterColumnOrRecreate(MySqlTable.EMPIRE_ALLIES, "ally", "allies", "VARCHAR(320) NOT NULL");
    }

    private void updateLandOwnersMissingCollumns() {
        addColumn(MySqlTable.SETTLEMENTS, "created", "BIGINT DEFAULT 0 AFTER description");
        addColumn(MySqlTable.EMPIRES, "created", "BIGINT DEFAULT 0 AFTER description");
        addColumn(MySqlTable.EMPIRES, "lastRename", "BIGINT DEFAULT 0 AFTER created");
    }

    @Override
    public void saveEmpire(Empire empire) {
        Utils.catchSync();
        empireQueue.add(empire.getId(), empire);
        empireQueue.finishAdding();
    }

    private void saveEmpire(Connection conn, Empire empire) {
        try {
            save(conn, empire, "members", "senators", "permissions");
            if (!empire.getId().isNull()) {
                updateDataMap(conn, "empire_vaults", "empireId", (Integer) empire.getId().value(), "content", "amount", empire.getVault());
                updateDataMap(conn, "empire_uses", "empireId", (Integer) empire.getId().value(), "type", "state",
                        empire.getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                updateDataMap(conn, "empire_perks", "empireId", (Integer) empire.getId().value(), "perk", "",
                        empire.getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
                updateDataMap(conn, "empire_permissions", "settlementId", (Integer) empire.getId().value(), "rank", "permission", empire.getRanksPermissions(), "value");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving empire " + empire.getName() + " to database!", e);
        }
    }

    @Override
    public void deleteEmpire(Empire empire) {
        Utils.catchSync();
        if (!empire.getId().isNull()) {
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "empires WHERE id=?")) {
                sta.setInt(1, (Integer) empire.getId().value());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete empire " + empire.getId().value() + "/" + empire.getName() + " from database!", e);
            }
        } else {
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "empires WHERE name LIKE ? LIMIT 1")) {
                sta.setString(1, empire.getName());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete empire " + empire.getId().value() + "/" + empire.getName() + " from database!", e);
            }
        }
    }

    @Override
    public void saveVillage(Village village) {
        Utils.catchSync();
        villageQueue.add(village.getId(), village);
        villageQueue.finishAdding();
    }

    private void saveVillage(Connection conn, Village village) {
        try {
            save(conn, village, "residents", "permissions");
            if (!village.getId().isNull()) {
                updateDataMap(conn, "settlement_vaults", "settlementId", (Integer) village.getId().value(), "content", "amount", village.getVault());
                updateDataMap(conn, "settlement_outposts", "settlementId", (Integer) village.getId().value(), "name", "", village.getOutpostHomes());
                updateDataMap(conn, "settlement_uses", "settlementId", (Integer) village.getId().value(), "type", "state",
                        village.getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                updateDataMap(conn, "settlement_perks", "settlementId", (Integer) village.getId().value(), "perk", "",
                        village.getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
                updateDataMap(conn, "settlement_permissions", "settlementId", (Integer) village.getId().value(), "rank", "permission", village.getRanksPermissions(), "value");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving village " + village.getName() + " to database!", e);
        }
    }

    @Override
    public void saveVillages(Collection<Village> villages) {
        Utils.catchSync();
        for (Village village : villages) {
            villageQueue.add(village.getId(), village);
        }
        villageQueue.finishAdding();
    }

    @Override
    public void deleteVillage(Village village) {
        Utils.catchSync();
        if (!village.getId().isNull()) {
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "settlements WHERE id=?")) {
                sta.setInt(1, (Integer) village.getId().value());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + village.getId().value() + "/" + village.getName() + " from database!", e);
            }
        } else {
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "settlements WHERE name LIKE ? LIMIT 1")) {
                sta.setString(1, village.getName());
                sta.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + village.getId().value() + "/" + village.getName() + " from database!", e);
            }
        }
    }

    @Override
    public void unclaimLand(OwnedLand... lands) {
        Utils.catchSync();
        if (lands.length == 0) {
            return;
        }
        try (Connection conn = ds.getConnection();
             PreparedStatement idSta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE id = ?;");
             PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE world = ? AND x = ? AND z = ?;")) {
            for (OwnedLand land : lands) {
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
        }
    }

    @Override
    public void unclaimLand(Village village) {
        Utils.catchSync();
        if (village.getId().value() == null) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims of settlement " + village.getName() + " from database! The village has no ID?");
            return;
        }

        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE settlementId = ?;")) {
            sta.setInt(1, (Integer) village.getId().value());
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims of settlement " + village.getName() + " from database! ", e);
        }
    }

    @Override
    public void unclaimLand(String worldName) {
        Utils.catchSync();

        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE world = ?;")) {
            sta.setString(1, worldName);
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims of world " + worldName + " from database! ", e);
        }
    }

    @Override
    public void renameLandWorld(String oldName, String newName) {
        Utils.catchSync();

        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement("UPDATE " + tablePrefix + "claims SET world = ? WHERE world = ?;")) {
            sta.setString(1, newName);
            sta.setString(2, oldName);
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to change claims world from " + oldName + " to " + newName + " in database! ", e);
        }
    }

    @Override
    public void saveLand(OwnedLand... lands) {
        Utils.catchSync();
        for (OwnedLand land : lands) {
            landQueue.add(land.getId(), land);
        }
        landQueue.finishAdding();
    }

    private void saveLand(Connection conn, OwnedLand land) {
        try {
            save(conn, land, "landOwner", "uses", "modes");
            if (!land.getId().isNull()) {
                updateDataMap(conn, "claim_uses", "claimId", (Integer) land.getId().value(), "type", "state",
                        land.getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                updateDataMap(conn, "claim_modes", "claimId", (Integer) land.getId().value(), "mode", "enabled",
                        land.getModes().entrySet().stream()
                                .filter(e -> e.getKey().appliesTo(PlotMode.AppliesTo.LAND_OWNER))
                                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving claim " + land.getKey() + " to database!", e);
        }
    }

    @Override
    public Collection<OwnedLand> getAllLand() {
        Utils.catchSync();
        String sql = "SELECT c.*, r.uuid AS `owner` FROM " + tablePrefix + "claims AS c LEFT JOIN " + tablePrefix + "residents AS r ON c.ownerId = r.id;";
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
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
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void deleteLand(IdObject idObject) {
        Utils.catchSync();
        String sql = "DELETE FROM " + tablePrefix + "claims WHERE id = ?;";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, (Integer) idObject.value());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims " + idObject + " from database!", e);
        }
    }

    @Override
    public Collection<Village> getAllVillages() {
        Utils.catchSync();
        String sql = "SELECT s.*, r.uuid AS chief FROM " + tablePrefix + "settlements AS s LEFT JOIN " + tablePrefix + "residents AS r ON s.chiefId = r.id;";
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
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
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<Empire> getAllEmpires() {
        Utils.catchSync();
        String sql = "SELECT * FROM " + tablePrefix + "empires;";
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
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
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Resident getResident(UUID uuid) {
        Utils.catchSync();
        String sql = "SELECT * FROM " + tablePrefix + "residents WHERE uuid=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setString(1, uuid.toString());
            ResultSet rs = sta.executeQuery();
            if (rs.next()) {
                return createResident(conn, rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get resident information for player with UUID " + uuid + " from database!", e);
        }
        return null;
    }

    @Override
    public Resident getResident(String name) {
        Utils.catchSync();
        String sql = "SELECT * FROM " + tablePrefix + "residents WHERE name LIKE ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setString(1, name);
            ResultSet rs = sta.executeQuery();
            if (rs.next()) {
                return createResident(conn, rs);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get resident information for player with name " + name + " from database!", e);
        }
        return null;
    }

    @Override
    public void saveResident(Resident... residents) {
        Utils.catchSync();
        for (Resident resident : residents) {
            residentQueue.add(IdObject.get(resident.getId()), resident);
        }
        residentQueue.finishAdding();
    }

    private void saveResident(Connection conn, Resident resident) {
        Map<String, Object> values = getValues(resident.serialize());
        String sql = "INSERT INTO " + tablePrefix + "residents "
                + "(`" + String.join("`,`", values.keySet()) + "`)"
                + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")"
                + " ON DUPLICATE KEY UPDATE " + values.keySet().stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
        try {
            PreparedStatement sta = conn.prepareStatement(sql, resident.getId() == null ? PreparedStatement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
            int i = 0;
            for (Object value : values.values()) {
                i++;
                sta.setObject(i, value);
            }
            sta.executeUpdate();
            if (resident.getId() == null) {
                ResultSet rs = sta.getGeneratedKeys();
                if (rs.next()) {
                    resident.setId(rs.getInt(1));
                }
            }
            if (resident.getId() != null) {
                updateList(conn, "resident_channels", "userId", (Integer) resident.getId(), "channel", resident.getChannels().stream().map(ChatChannel::name).collect(Collectors.toList()));
                updateUserList(conn, "resident_friends", "userId", (Integer) resident.getId(), "friendId", resident.getFriends());
                updateUserList(conn, "resident_ignores", "userId", (Integer) resident.getId(), "ignoredId", resident.getIgnore());
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving player " + resident.getName() + "/" + resident.getUUID() + " to database!", e);
        }
    }

    @Override
    public void saveLocks(Lock... locks) {
        Utils.catchSync();
        for (Lock lock : locks) {
            if (lock.isValid()) {
                lockQueue.add(lock.getId(), lock);
            }
        }
        lockQueue.finishAdding();
    }

    @Override
    public void deleteLocks(Lock... locks) {
        Utils.catchSync();
        if (locks.length == 0) {
            return;
        }
        try (Connection conn = ds.getConnection();
             PreparedStatement idSta = conn.prepareStatement("DELETE FROM " + tablePrefix + "locks WHERE id = ?;");
             PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "locks WHERE world = ? AND x = ? AND y = ? AND z = ?;")) {
            for (Lock lock : locks) {
                if (lock.getId().isNull()) {
                    sta.setString(1, lock.getLocation().getWorld());
                    sta.setInt(2, lock.getLocation().getX());
                    sta.setInt(3, lock.getLocation().getY());
                    sta.setInt(4, lock.getLocation().getZ());
                    sta.addBatch();
                } else {
                    idSta.setObject(1, lock.getId().value());
                    idSta.addBatch();
                }
            }
            idSta.executeBatch();
            sta.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + locks.length + " locks from database!", e);
        }
    }

    @Override
    public void deleteLocks(String worldName) {
        Utils.catchSync();
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "locks WHERE world = ?;")) {
            sta.setString(1, worldName);
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete locks in world " + worldName + " from database!", e);
        }
    }

    @Override
    public Collection<Lock> getAllLocks() {
        Utils.catchSync();
        String sql = "SELECT l.*, r.uuid AS `owner` FROM " + tablePrefix + "locks AS l LEFT JOIN " + tablePrefix + "residents AS r ON l.ownerId = r.id;";
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
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
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<Lock> getLocks(SimpleChunkLocation... chunks) {
        if (chunks.length == 0) {
            return  Collections.EMPTY_LIST;
        }
        Utils.catchSync();
        String sql = "SELECT l.*, r.uuid AS `owner` FROM " + tablePrefix + "locks AS l" +
                " LEFT JOIN " + tablePrefix + "residents AS r ON l.ownerId = r.id" +
                " WHERE world = ? AND x BETWEEN ? AND ? AND z BETWEEN ? AND ?;";
        List<Lock> locks = new ArrayList<>();
        try (Connection conn = ds.getConnection(); PreparedStatement sta = conn.prepareStatement(sql)) {
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
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement("INSERT INTO " + tablePrefix + "logs " +
                     "(`empireId`, `settlementId`, `residentId`, `action`, `type`, `amount`) " +
                     "VALUES (?, ?, ?, ?, ?, ?);"
             )
        ) {
            for (Map.Entry<String, Long> value : values.entrySet()) {
                if (value.getValue() != 0) {
                    sta.setInt(1, empire != null ? (Integer) empire.getId().value() : -1);
                    sta.setInt(2, village != null ? (Integer) village.getId().value() : -1);
                    sta.setInt(3, resident != null && resident.getId() != null ? (Integer) resident.getId() : -1);
                    sta.setString(4, action.toLowerCase());
                    sta.setString(5, value.getKey().toLowerCase());
                    sta.setLong(6, value.getValue());
                    sta.addBatch();
                }
            }
            sta.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
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
            sql += " AND l.`action` = 'deposit'";
        }
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setInt(1, (Integer) id.value());
            ResultSet rs = sta.executeQuery();
            if (rs.next()) {
                return (int) Math.ceil(rs.getInt(1) / (double) Utils.getEntriesPerPage());
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get log page count for " + id, e);
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
        villageQueue.saving.run();
        empireQueue.saving.run();
        residentQueue.saving.run();
        landQueue.saving.run();
        lockQueue.saving.run();
        ds.close();
    }

    private List<String[]> getLog(String column, IdObject id, Resident resident, int page, LogType type) {
        List<String[]> log = new ArrayList<>();
        if (id.isNull() || page < 1) {
            return log;
        }
        int start = (page-1) * Utils.getEntriesPerPage();
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
            sql += " AND `l.action` = 'deposit'";
        }
        sql += " ORDER BY l.id DESC LIMIT " + start + "," + Utils.getEntriesPerPage() + ";";
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement(sql)) {
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
                        "title", hasPlayer ? rank.getTitle(village, rs.getInt("playerTitle")) : "",
                        "action", rs.getString("action"),
                        "type", rs.getString("type"),
                        "icon", Utils.getIcon(resident, rs.getString("type")),
                        "amount", String.valueOf(amount),
                        "timestamp", Utils.TIME_FORMAT.format(new Date(rs.getTimestamp("timestamp").getTime()))
                });
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get log for " + id, e);
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

        Collection<String> senators = new ArrayList<>();
        for (int memberId : members) {
            PreparedStatement sSta = conn.prepareStatement("SELECT uuid FROM " + tablePrefix + "residents WHERE empirerank = ? AND settlementId = ?;");
            sSta.setString(1, EmpireRank.getLeader().next().name());
            sSta.setInt(2, memberId);
            ResultSet sRs = sSta.executeQuery();
            while (sRs.next()) {
                senators.add(sRs.getString("uuid"));
            }
        }
        map.put("senators", senators);

        map.put("banished", selectList(conn, "empire", id, "banished"));
        map.put("allies", selectList(conn, "empire", id, "allies"));

        map.put("uses", selectMap(conn, "empire_uses", "empireId", id, "type", "state", String.class));
        map.put("vault", selectMap(conn, "empire_vaults", "empireId", id, "content", "amount", Long.class));

        PreparedStatement pSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "empire_perks WHERE empireId = ?;");
        pSta.setInt(1, id);
        ResultSet pRs = pSta.executeQuery();
        Map<String, Map<String, Object>> unlockedPerks = new LinkedHashMap<>();
        while (pRs.next()) {
            unlockedPerks.put(pRs.getString("perk"), toMap(pRs));
        }
        map.put("perks", unlockedPerks);

        PreparedStatement rpSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "empire_permissions WHERE empireId = ?;");
        rpSta.setInt(1, id);
        ResultSet rpRs = rpSta.executeQuery();
        Map<String, Map<String, Boolean>> rankPermissions = new LinkedHashMap<>();
        while (rpRs.next()) {
            rankPermissions.computeIfAbsent(rpRs.getString("rank"), k -> new LinkedHashMap<>())
                    .put(rpRs.getString("permission"), rpRs.getBoolean("value"));
        }
        map.put("permissions", rankPermissions);

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

        PreparedStatement oSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_outposts WHERE settlementId = ?;");
        oSta.setInt(1, id);
        ResultSet oRs = oSta.executeQuery();
        Map<String, Map<String, Object>> outposts = new LinkedHashMap<>();
        while (oRs.next()) {
            outposts.put(oRs.getString("name"), toMap(oRs));
        }
        map.put("outposts", outposts);

        PreparedStatement pSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_perks WHERE settlementId = ?;");
        pSta.setInt(1, id);
        ResultSet pRs = pSta.executeQuery();
        Map<String, Map<String, Object>> unlockedPerks = new LinkedHashMap<>();
        while (pRs.next()) {
            unlockedPerks.put(pRs.getString("perk"), toMap(pRs));
        }
        map.put("perks", unlockedPerks);

        PreparedStatement rpSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_permissions WHERE settlementId = ?;");
        rpSta.setInt(1, id);
        ResultSet rpRs = rpSta.executeQuery();
        Map<String, Map<String, Boolean>> rankPermissions = new LinkedHashMap<>();
        while (rpRs.next()) {
            rankPermissions.computeIfAbsent(rpRs.getString("rank"), k -> new LinkedHashMap<>())
                    .put(rpRs.getString("permission"), rpRs.getBoolean("value"));
        }
        map.put("permissions", rankPermissions);

        return Village.deserialize(map);
    }

    private Resident createResident(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        int id = rs.getInt("id");
        map.put("channels", selectList(conn, "resident_channels", "userId", id, "channel"));
        map.put("friends", selectResidentList(conn, "resident_friends", "userId", id, "friendId"));
        map.put("ignores", selectResidentList(conn, "resident_ignores", "userId", id, "ignoredId"));
        return Resident.deserialize(map);
    }

    private OwnedLand createLand(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        int id = rs.getInt("id");
        map.put("modes", selectMap(conn, "claim_modes", "claimId", id, "mode", "enabled", Boolean.class));
        map.put("uses", selectMap(conn, "claim_uses", "claimId", id, "type", "state", String.class));
        return OwnedLand.deserialize(map);
    }

    private Lock createLock(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        map.put("allowed", selectList(conn, "lock_allowed", "lockId", rs.getInt("id"), "allowed"));
        return Lock.deserialize(map);
    }

    // ------ Database operation helpers ----- //

    private IdObject save(Connection conn, DatabaseSerializable serializable, String... ignored) throws SQLException {
        Map<String, Object> serialized = serializable.serialize();
        Map<String, Object> values = getValues(serialized, ignored);
        String sql = "INSERT INTO " + tablePrefix + serializable.getDbName() + "s "
                + "(`" + String.join("`,`", values.keySet()) + "`)"
                + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")"
                + " ON DUPLICATE KEY UPDATE " + values.keySet().stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
        PreparedStatement sta = conn.prepareStatement(sql, serializable.getId().isNull() ? PreparedStatement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
        int i = 0;
        for (Object value : values.values()) {
            i++;
            sta.setObject(i, value);
        }
        sta.executeUpdate();
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
        deleteObjects.put(1, keyId);
        int i = 2;
        if (values.isEmpty()) {
            deleteSql = "DELETE FROM " + tablePrefix + tableName +
                    " WHERE " + keyColumn + " = ?;";
        } else if (values.values().iterator().next() instanceof Collection) {
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
                deleteSql += " AND " + mapKey + " NOT IN " +
                        "(" + String.join(",", Collections.nCopies(map.size(), "?")) + ")";
                for (String key : map.keySet()) {
                    deleteObjects.put(i, key);
                    i++;
                }
                List<String> rankQuery = new ArrayList<>();
                for (Map.Entry<String, Collection<Object>> entry : map.entrySet()) {
                    rankQuery.add(mapKey + " = ?" +
                            " AND " + mapValue + " NOT IN " +
                            "(" + String.join(",", Collections.nCopies(entry.getValue().size(), "?")) + ")");
                    deleteObjects.put(i, entry.getKey());
                    i++;
                    for (Object o : entry.getValue()) {
                        deleteObjects.put(i, o);
                        i++;
                    }
                }
                deleteSql += " OR ((" + String.join(") OR (", rankQuery) + "))";
            }
            deleteSql += ";";
        } else {
            deleteSql = "DELETE FROM " + tablePrefix + tableName +
                    " WHERE " + keyColumn + " = ?" +
                    " AND " + mapKey + " NOT IN " +
                    "(" + String.join(",", Collections.nCopies(values.size(), "?")) + ");";
            for (String key : values.keySet()) {
                deleteObjects.put(i, key);
                i++;
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
            String insertSql = "INSERT IGNORE INTO " + tablePrefix + tableName + " (" + keyColumn + ", " + mapKey + ", " + String.join(",", columns) + ")" +
                    " VALUES (?,?," + String.join(",", Collections.nCopies(columns.size(), "?")) + ")" +
                    " ON DUPLICATE KEY UPDATE " + columns.stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
            PreparedStatement insertSta = conn.prepareStatement(insertSql);
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                if (entry.getValue() instanceof Collection) {
                    for (Object o : (Collection<Object>) entry.getValue()) {
                        insertSta.setInt(1, keyId);
                        insertSta.setObject(2, entry.getKey());
                        insertSta.setObject(3, o);
                        insertSta.addBatch();
                    }
                } else if (entry.getValue() instanceof Map && mapValueColumns.length > 0) {
                    for (Map.Entry<Object, Object> o : ((Map<Object, Object>) entry.getValue()).entrySet()) {
                        insertSta.setInt(1, keyId);
                        insertSta.setObject(2, entry.getKey());
                        insertSta.setObject(3, o.getKey());
                        insertSta.setObject(4, o.getValue());
                        insertSta.addBatch();
                    }
                } else {
                    insertSta.setInt(1, keyId);
                    insertSta.setObject(2, entry.getKey());

                    int j = 3;
                    Map<String, Object> map = null;
                    if (entry.getValue() instanceof ConfigurationSerializable) {
                        map = ((ConfigurationSerializable) entry.getValue()).serialize();
                    } else if (entry.getValue() instanceof Map) {
                        map = (Map<String, Object>) entry.getValue();
                    }
                    if (map != null) {
                        Map<String, Object> serMap = getValues(map, mapKey);
                        for (String column : columns) {
                            insertSta.setObject(j, serMap.get(column));
                            j++;
                        }
                    } else {
                        insertSta.setObject(j, entry.getValue());
                    }
                    insertSta.addBatch();
                }
            }
            insertSta.executeBatch();
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
            deleteSql = "DELETE t FROM " + tablePrefix + tableName + " AS t" +
                    " WHERE t." + keyColumn + " = ?" +
                    " AND t." + entryColumn + " NOT IN (" + String.join(",", Collections.nCopies(entries.size(), "?")) + ");";
        }

        PreparedStatement deleteSta = conn.prepareStatement(deleteSql);
        deleteSta.setInt(1, keyId);
        int i = 2;
        for (Object entry : entries) {
            deleteSta.setObject(i, entry);
            i++;
        }
        deleteSta.executeUpdate();

        if (!entries.isEmpty()) {
            String insertSql = "INSERT IGNORE INTO " + tablePrefix + tableName + " (" + keyColumn + ", " + entryColumn + ")" +
                    " VALUES (?,?);";
            PreparedStatement insertSta = conn.prepareStatement(insertSql);
            for (Object entry : entries) {
                insertSta.setInt(1, keyId);
                insertSta.setObject(2, entry);
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
            deleteSta.setString(i, id.toString());
            i++;
        }
        deleteSta.executeUpdate();

        if (!users.isEmpty()) {
            String insertSql = "INSERT IGNORE INTO " + tablePrefix + tableName + " (" + keyColumn + ", " + usersColumn + ")" +
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

    private void alterOrRecreate(MySqlTable table, String alterSql) {
        String sql = "SELECT * FROM " + tablePrefix + table.getDbName() + " LIMIT 1";
        try (Connection conn = ds.getConnection();
             Statement sta = conn.createStatement()) {
            ResultSet rs = sta.executeQuery(sql);
            if (rs.next()) {
                sta.execute(alterSql);
            } else {
                sta.execute("DROP TABLE " + tablePrefix + table.getDbName());
                sta.execute(table.getSql(tablePrefix));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to alter table " + table.getDbName(), e);
        }
    }

    private void alterColumnOrRecreate(MySqlTable table, String oldColumn, String newColumn, String columnDefinition) {
        String sql = "SELECT * FROM " + tablePrefix + table.getDbName() + " LIMIT 1;";
        try (Connection conn = ds.getConnection();
             Statement sta = conn.createStatement()) {
            ResultSet rs = sta.executeQuery(sql);
            if (rs.next()) {
                try {
                    rs.getObject(newColumn);
                } catch (SQLException columnNotFound) {
                    sta.execute("ALTER TABLE `" + tablePrefix + table.getDbName() + "` CHANGE COLUMN `" + oldColumn + "` `" + newColumn + "` " + columnDefinition + ";");
                }
            } else {
                sta.addBatch("SET FOREIGN_KEY_CHECKS = 0");
                sta.addBatch("DROP TABLE " + tablePrefix + table.getDbName() + ";");
                sta.addBatch(table.getSql(tablePrefix));
                sta.addBatch("SET FOREIGN_KEY_CHECKS = 1");
                sta.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to alter table " + table.getDbName(), e);
        }
    }

    private void addColumn(MySqlTable table, String column, String definition) {
        try (Connection conn = ds.getConnection();
             Statement sta = conn.createStatement()) {
            sta.execute("ALTER TABLE `" + tablePrefix + table.getDbName() + "` ADD COLUMN `" + column + "` " + definition + ";");
        } catch (SQLException ignored) {
            // column exists
        }
    }

    // ------ Utility methods ----- //

    private Map<String, Object> getValues(Map<String, ?> serialized, String... ignoredKeys) {
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
                        id = resident != null ? resident.getId() : null;
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

    private void initConnectionClass(HikariDataSource ds) throws RuntimeException {
        String dataSourceClassName = tryDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        if (dataSourceClassName == null) {
            dataSourceClassName = tryDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        }
        if (dataSourceClassName != null) {
           plugin.getLogger().log(Level.INFO, "Using " + dataSourceClassName + " database source");
            ds.setDataSourceClassName(dataSourceClassName);
        }

        if (dataSourceClassName == null) {
            String driverClassName = tryDriverClassName("org.mariadb.jdbc.Driver");
            if (driverClassName == null) {
                driverClassName = tryDriverClassName("com.mysql.cj.jdbc.Driver");
            }
            if (driverClassName == null) {
                driverClassName = tryDriverClassName("com.mysql.jdbc.Driver");
            }

            if (driverClassName != null) {
                plugin.getLogger().log(Level.INFO, "Using " + driverClassName + " database driver");
                ds.setDriverClassName(driverClassName);
            } else {
                throw new RuntimeException("Could not find database driver or data source class! Plugin wont work without a database!");
            }
        }
    }

    private String tryDriverClassName(String className) {
        try {
            Class.forName(className).newInstance();
            return className;
        } catch (Exception ignored) {}
        return null;
    }

    private String tryDataSourceClassName(String className) {
        try {
            Class.forName(className);
            return className;
        } catch (Exception ignored) {}
        return null;
    }

    public static class SaveQueue<T> {
        private HikariDataSource ds;
        private BiConsumer<Connection, T> handleSave;

        private BukkitTask task = null;
        private final Map<IdObject, T> queue = new ConcurrentHashMap<>();
        private Runnable saving = () -> {
            synchronized (queue) {
                forceSave = false;
                if (task != null) {
                    task.cancel();
                    task = null;
                }
                if (!queue.isEmpty()) {
                    String name = null;
                    try (Connection conn = ds.getConnection()) {
                        for (T t : queue.values()) {
                            handleSave.accept(conn, t);
                            if (name == null) {
                                name = t.getClass().getSimpleName();
                            }
                        }
                        queue.clear();
                    } catch (SQLException e) {
                        KingdomsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while saving " + queue.size() + " " + (name != null ? name : "object") + "s to database!", e);
                    }
                }
            }
        };
        private int maxSize;
        private int saveWait;
        private boolean forceSave = false;

        public SaveQueue(HikariDataSource ds, int maxSize, int saveWait, BiConsumer<Connection, T> handleSave) {
            this.ds = ds;
            this.maxSize = maxSize;
            this.saveWait = saveWait;
            this.handleSave = handleSave;
        }

        public void add(IdObject id, T object) {
            if (id.isNull()) {
                id = IdObject.get(object.hashCode());
                forceSave = true;
            }
            synchronized (queue) {
                queue.put(id, object);
            }
            if (queue.size() >= maxSize) {
                saving.run();
            } else if (!forceSave && task == null) {
                task = Bukkit.getScheduler().runTaskLaterAsynchronously(KingdomsPlugin.getInstance(), saving, saveWait * 20);
            }
        }

        /**
         * Finish up adding to this queue and check if we should save or not
         * @return Whether or not a save was run due to finishing up
         */
        public boolean finishAdding() {
             if (forceSave || queue.size()  > maxSize / 2) {
                 saving.run();
                 return true;
             }
             return false;
        }
    }
}
