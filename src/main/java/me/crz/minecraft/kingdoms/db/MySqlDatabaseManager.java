package me.crz.minecraft.kingdoms.db;

import com.zaxxer.hikari.HikariDataSource;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MySqlDatabaseManager extends DatabaseManager {
    private HikariDataSource ds;
    private String tablePrefix;
    private final KingdomsPlugin plugin = KingdomsPlugin.getInstance();

    @Override
    public void connect() throws Exception {
        ConfigurationSection config = KingdomsPlugin.getInstance().getConfig().getConfigurationSection("storage");
        tablePrefix = config.getString("table-prefix");
        String host = config.getString("host");
        int port = config.getInt("port");
        String database = config.getString("database");
        String urlPar = config.getString("url-parameters");

        ds = new HikariDataSource();
        initConnectionClass(ds);

        ds.addDataSourceProperty("url", "jdbc:mysql://" + host + ":" + port + "/" + database + urlPar);
        ds.setUsername(config.getString("user"));
        ds.setPassword(config.getString("password"));
        ds.setConnectionTimeout(5000);

        initializeTables();
    }

    private void initializeTables() throws SQLException {
        try (Connection conn = ds.getConnection(); Statement sta = conn.createStatement()) {
            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlements ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "name VARCHAR(255) NOT NULL, INDEX (name),"
                    + "chiefId INTEGER NOT NULL,"
                    + "FOREIGN KEY (chiefId) REFERENCES " + tablePrefix + "residents(id) ON DELETE RESTRICT,"
                    + "level INTEGER NOT NULL,"
                    + "settlementState VARCHAR(255) NOT NULL,"
                    + "enterMessage TEXT NOT NULL,"
                    + "leaveMessage TEXT NOT NULL,"
                    + "buildState VARCHAR(255) NOT NULL,"
                    + "pvp BOOLEAN NOT NULL,"
                    + "fireSpread BOOLEAN NOT NULL,"
                    + "explosions BOOLEAN NOT NULL,"
                    + "mobSpawning BOOLEAN NOT NULL,"
                    + "buff BOOLEAN NOT NULL,"
                    + "teleporter BOOLEAN NOT NULL,"
                    + "teleporterLoc_world VARCHAR(255),"
                    + "teleporterLoc_x DOUBLE,"
                    + "teleporterLoc_y DOUBLE,"
                    + "teleporterLoc_z DOUBLE,"
                    + "teleporterLoc_yaw FLOAT,"
                    + "teleporterLoc_pitch FLOAT,"
                    + "bank INTEGER,"
                    + "homeLoc_world VARCHAR(255),"
                    + "homeLoc_x DOUBLE,"
                    + "homeLoc_y DOUBLE,"
                    + "homeLoc_z DOUBLE,"
                    + "homeLoc_yaw FLOAT,"
                    + "homeLoc_pitch FLOAT,"
                    + "lastUpkeep BIGINT(11) NOT NULL,"
                    + "upkeepStage VARCHAR(255) NOT NULL"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_outposts ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER, INDEX (settlementId),"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "name VARCHAR(255),"
                    + "UNIQUE (settlementId, name),"
                    + "world VARCHAR(255),"
                    + "x DOUBLE,"
                    + "y DOUBLE,"
                    + "z DOUBLE,"
                    + "yaw FLOAT,"
                    + "pitch FLOAT"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_vaults ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER,"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "content VARCHAR(255) NOT NULL,"
                    + "amount INTEGER"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_uses ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER, INDEX(claimId),"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "use VARCHAR(255) NOT NULL,"
                    + "state VARCHAR(255) NOT NULL,"
                    + "UNIQUE (claimId, use, state)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_banished ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER, INDEX (settlementId),"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "banishedId INTEGER,"
                    + "FOREIGN KEY (banishedId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "UNIQUE (settlementId, banishedId)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_allies ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER, INDEX (settlementId),"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "allyId INTEGER,"
                    + "FOREIGN KEY (allyId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "UNIQUE (settlementId, allyId)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "residents ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "uuid CHAR(36) NOT NULL, UNIQUE (uuid),"
                    + "name VARCHAR(16) NOT NULL,"
                    + "settlementId INTEGER,"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE SET NULL,"
                    + "lastLogin BIGINT(11) NOT NULL,"
                    + "activeChannel VARCHAR(255) NOT NULL,"
                    + "swear BOOLEAN NOT NULL"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "resident_channels ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "userId INTEGER, INDEX (userId),"
                    + "FOREIGN KEY (userId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "channel VARCHAR(255),"
                    + "UNIQUE (userId, channel)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "resident_friends ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "userId INTEGER, INDEX (userId),"
                    + "FOREIGN KEY (userId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "friendId INTEGER,"
                    + "FOREIGN KEY (friendId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "UNIQUE (userId, friendId)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "resident_ignores ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "userId INTEGER, INDEX (userId),"
                    + "FOREIGN KEY (userId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "ignoredId INTEGER,"
                    + "FOREIGN KEY (ignoredId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "UNIQUE (userId, ignoredId)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "claims ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER NOT NULL, INDEX (settlementId), "
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE RESTRICT,"
                    + "world VARCHAR(255),INDEX(world),"
                    + "x INTEGER,INDEX(x),"
                    + "z INTEGER,INDEX(z),"
                    + "UNIQUE(world, x, z),"
                    + "ownerId INTEGER,"
                    + "FOREIGN KEY (ownerId) REFERENCES " + tablePrefix + "residents(id) ON DELETE SET NULL,"
                    + "price INTEGER DEFAULT 0,"
                    + "forSale BOOLEAN NOT NULL,"
                    + "pvp BOOLEAN NOT NULL,"
                    + "fireSpread BOOLEAN NOT NULL,"
                    + "explosions BOOLEAN NOT NULL,"
                    + "mobSpawning BOOLEAN NOT NULL,"
                    + "build BOOLEAN NOT NULL,"
                    + "isOutpost BOOLEAN NOT NULL, "
                    + "BUILDSTATE VARCHAR(255) NOT NULL "
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.execute("CREATE TABLE IF NOT EXISTS " + tablePrefix + "claim_uses ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "claimId INTEGER, INDEX(claimId),"
                    + "FOREIGN KEY (claimId) REFERENCES " + tablePrefix + "claims(id) ON DELETE CASCADE,"
                    + "use VARCHAR(255) NOT NULL,"
                    + "state VARCHAR(255) NOT NULL,"
                    + "UNIQUE (claimId, use, state)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");
        }
    }

    @Override
    public void saveVillage(Village village) {
        Utils.catchSync();
        Map<String, Object> values = getValues(village.serialize());
        String sql = "INSERT INTO " + tablePrefix + "settlements "
                + "(`" + String.join("`,`", values.keySet()) + "`)"
                + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")"
                + " ON DUPLICATE KEY UPDATE " + values.keySet().stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            int i = 0;
            for (Object value : values.values()) {
                i++;
                sta.setObject(i, value);
            }
            sta.executeUpdate();
            if (village.getId().value() == null) {
                ResultSet rs = sta.getGeneratedKeys();
                if (rs.next()) {
                    village.setId(IdObject.get(rs));
                }
            }
            if (village.getId().value() != null) {
                updateUserList(conn, "settlement_banished", "settlementId", (Integer) village.getId().value(), "banishedId", village.getBanishedResidents());
                updateAllyList(conn, "settlement_allies", "settlementId", (Integer) village.getId().value(), "banishedId", village.getAllies());
                updateDataMap(conn, "settlement_vaults", "settlementId", (Integer) village.getId().value(), "content", "amount", village.getVault());
                updateDataMap(conn, "settlement_outposts", "settlementId", (Integer) village.getId().value(), "name", "", village.getOutpostHomes());
                updateDataMap(conn, "settlement_uses", "settlementId", (Integer) village.getId().value(), "use", "state",
                        village.getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving village " + village.getName() + " to database!", e);
        }
    }

    @Override
    public void deleteVillage(Village village) {
        Utils.catchSync();
        if (village.getId() != null) {
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "settlements WHERE id=?")) {
                sta.setInt(1, (Integer) village.getId().value());
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + village.getId().value() + "/" + village.getName() + " from database!", e);
            }
        } else {
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "settlements WHERE name LIKE ? LIMIT 1")) {
                sta.setString(1, village.getName());
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete " + village.getId().value() + "/" + village.getName() + " from database!", e);
            }
        }
    }

    @Override
    public void saveVillages(Collection<Village> villages) {
        Utils.catchSync();
        for (Village village : villages) {
            saveVillage(village);
        }
    }

    @Override
    public void unclaimLand(OwnedLand... lands) {
        Utils.catchSync();
        try (Connection conn = ds.getConnection();
            PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE world = ? AND x = ? AND z = ?;")) {
            for (OwnedLand land : lands) {
                sta.setString(1, land.getWorldName());
                sta.setInt(2, land.getX());
                sta.setInt(3, land.getZ());
                sta.addBatch();
            }
            sta.executeUpdate();
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
             PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE ownerSettlement = ?;")) {
            sta.setInt(1, (Integer) village.getId().value());
            sta.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to delete claims of settlement " + village.getName() + " from database! ", e);
        }
    }

    @Override
    public void saveLand(OwnedLand... lands) {
        Utils.catchSync();
        if (lands.length == 0) {
            return;
        }
        for (OwnedLand land : lands) {
            Map<String, Object> values = getValues(lands[0].serialize());
            String sql = "INSERT INTO " + tablePrefix + "claims "
                    + "(`" + String.join("`,`", values.keySet()) + "`)"
                    + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")"
                    + " ON DUPLICATE KEY UPDATE " + values.keySet().stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement(sql)) {
                int i = 0;
                for (Object value : values.values()) {
                    i++;
                    sta.setObject(i, value);
                }
                sta.executeUpdate();
                if (land.getId() == null) {
                    ResultSet rs = sta.getGeneratedKeys();
                    if (rs.next()) {
                        land.setId(IdObject.get(rs.getInt(1)));
                    }
                }
                if (land.getId() != null) {
                    updateDataMap(conn, "claim_uses", "claimId", (Integer) land.getId().value(), "use", "state",
                            land.getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while saving claim " + land.getLocation().getKey() + " to database!", e);
            }
        }

    }

    @Override
    public Collection<OwnedLand> getAllLand() {
        Utils.catchSync();
        String sql = "SELECT c.*, r.id AS plotOwner FROM " + tablePrefix + "claims AS c LEFT JOIN " + tablePrefix + "residents AS r ON c.ownerId = r.id;";
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            List<OwnedLand> claims = new ArrayList<>();
            while (rs.next()) {
                claims.add(createLand(conn, rs));
            }
            return claims;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all claims from database!", e);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<Village> getAllVillages() {
        Utils.catchSync();
        String sql = "SELECT s.*, r.uuid AS chief FROM " + tablePrefix + "settlements AS s LEFT JOIN " + tablePrefix + "residents AS r ON s.chiefId = r.id;";
        try (Connection conn = ds.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            List<Village> villages = new ArrayList<>();
            while (rs.next()) {
                villages.add(createVillage(conn, rs));
            }
            return villages;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to get all settlements from database!", e);
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
        if (residents.length == 0) {
            return;
        }
        for (Resident resident : residents) {
            Map<String, Object> values = getValues(residents[0].serialize());
            String sql = "INSERT INTO " + tablePrefix + "residents "
                    + "(`" + String.join("`,`", values.keySet()) + "`)"
                    + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")"
                    + " ON DUPLICATE KEY UPDATE " + values.keySet().stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
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
                    updateUserList(conn, "resident_friends", "userId", (Integer) resident.getId(), "friendId", resident.getFriends());
                    updateUserList(conn, "resident_ignores", "userId", (Integer) resident.getId(), "ignoredId", resident.getIgnore());
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while saving player " + resident.getName() + "/" + resident.getUUID() + " to database!", e);
            }
        }
    }

    // ------ Creation methods ----- //

    private Village createVillage(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Map<String, Object> map = toMap(rs);

        PreparedStatement oSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_outposts WHERE settlementId = ?;");
        oSta.setInt(1, id);
        ResultSet oRs = oSta.executeQuery();
        Map<String, Map<String, Object>> outposts = new LinkedHashMap<>();
        while (oRs.next()) {
            outposts.put(oRs.getString("name"), toMap(oRs));
        }
        map.put("outposts", outposts);

        map.put("banished", selectResidentList(conn, "settlement_banished", "settlementId", rs.getInt("id"), "banishedId"));

        PreparedStatement uSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_uses WHERE settlementId = ?;");
        uSta.setInt(1, id);
        ResultSet uRs = uSta.executeQuery();
        Map<String, String> use = new LinkedHashMap<>();
        while (uRs.next()) {
            use.put(uRs.getString("use"), uRs.getString("state"));
        }
        map.put("uses", use);

        PreparedStatement vSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_vaults WHERE settlementId = ?;");
        vSta.setInt(1, id);
        ResultSet vRs = vSta.executeQuery();
        Map<String, Integer> vaults = new LinkedHashMap<>();
        while (vRs.next()) {
            vaults.put(vRs.getString("content"), vRs.getInt("amount"));
        }
        map.put("vaults", vaults);

        return Village.deserialize(map);
    }

    private Resident createResident(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        int id = rs.getInt("id");
        map.put("channels", selectList(conn, "resident_channels", id, "channel"));
        map.put("friends", selectResidentList(conn, "resident_friends", "userId", id, "friendId"));
        map.put("ignores", selectResidentList(conn, "resident_ignores", "userId", id, "ignoredId"));
        return Resident.deserialize(map);
    }

    private OwnedLand createLand(Connection conn, ResultSet rs) throws SQLException {
        Map<String, Object> map = toMap(rs);
        int id = rs.getInt("id");

        PreparedStatement uSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "claim_uses WHERE claimId = ?;");
        uSta.setInt(1, id);
        ResultSet uRs = uSta.executeQuery();
        Map<String, String> use = new LinkedHashMap<>();
        while (uRs.next()) {
            use.put(uRs.getString("use"), uRs.getString("state"));
        }
        map.put("uses", use);

        return OwnedLand.deserialize(map);
    }

    // ------ Database operation helpers ----- //

    private void updateDataMap(Connection conn, String tableName, String keyColumn, int keyId, String mapKey, String mapValue, Map<String, ?> values) throws SQLException {
        String deleteSql;
        if (values.isEmpty()) {
            deleteSql = "DELETE FROM " + tablePrefix + tableName +
                    " WHERE " + keyColumn + " = ?;";
        } else {
            deleteSql = "DELETE FROM " + tablePrefix + tableName +
                    " WHERE " + keyColumn + " = ?" +
                    " AND " + mapKey + " NOT IN " +
                    "(" + String.join(",", Collections.nCopies(values.size(), "?")) + ");";
        }

        PreparedStatement deleteSta = conn.prepareStatement(deleteSql);
        deleteSta.setInt(1, keyId);
        int i = 2;
        for (Object key : values.keySet()) {
            deleteSta.setObject(i, key);
            i++;
        }
        deleteSta.executeUpdate();

        if (!values.isEmpty()) {
            Object value = values.values().iterator().next();
            Set<String> columns = new LinkedHashSet<>();
            if (value instanceof ConfigurationSerializable) {
                for (String column : getValues(((ConfigurationSerializable) value).serialize()).keySet()) {
                    if (mapValue.isEmpty()) {
                        columns.add(column);
                    } else {
                        columns.add(mapValue + "_" + column);
                    }
                }
            } else {
                columns.add(mapValue);
            }
            String insertSql = "INSERT IGNORE INTO " + tablePrefix + tableName + " (" + keyColumn + ", " + mapKey + ", " + String.join(",", columns) + ")" +
                    " VALUES (?,?," + String.join(",", Collections.nCopies(columns.size(), "?")) + ");";
            PreparedStatement insertSta = conn.prepareStatement(insertSql);
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                insertSta.setInt(1, keyId);
                insertSta.setObject(2, entry.getKey());

                if (columns.size() > 1) {
                    int j = 3;
                    Map<String, Object> serMap = getValues(((ConfigurationSerializable) entry.getValue()).serialize());
                    for (String column : columns) {
                        insertSta.setObject(j, serMap.get(column));
                        j++;
                    }
                } else {
                    insertSta.setObject(2, entry.getValue());
                }
                insertSta.addBatch();
            }
            insertSta.executeUpdate();
        }
    }

    private List<Object> selectList(Connection conn, String tableName, int userId, String columnName) throws SQLException {
        String sql = "SELECT " + columnName + " FROM " + tablePrefix + tableName + " WHERE f.userId=?;";
        List<Object> list = new ArrayList<>();
        PreparedStatement sta = conn.prepareStatement(sql);
        sta.setInt(1, userId);
        ResultSet rs = sta.executeQuery();
        while (rs.next()) {
            list.add(rs.getObject(columnName));
        }
        return list;
    }

    private Collection<String> selectResidentList(Connection conn, String tableName, String keyColumn, int userId, String columnName) throws SQLException {
        String sql = "SELECT r.uuid FROM " + tablePrefix + tableName + " AS f" +
                " JOIN " + tablePrefix + "residents AS r ON f." + columnName + " = r.id" +
                " WHERE f." + keyColumn + "=?";
        Set<String> uuids = new LinkedHashSet<>();
        PreparedStatement sta = conn.prepareStatement(sql);
        sta.setInt(1, userId);
        ResultSet rs = sta.executeQuery();
        while (rs.next()) {
            uuids.add(rs.getString(1));
        }
        return uuids;
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
            insertSta.executeUpdate();
        }
    }

    private void updateAllyList(Connection conn, String tableName, String keyColumn, int keyId, String allyColumn, Collection<IdObject> allies) throws SQLException {
        String deleteSql;
        if (allies.isEmpty()) {
            deleteSql = "DELETE FROM " + tablePrefix + tableName + " " +
                    " WHERE " + keyColumn + " = ?;";
        } else {
            deleteSql = "DELETE t FROM " + tablePrefix + tableName + " AS t" +
                    " WHERE t." + keyColumn + " = ?" +
                    " AND t." + allyColumn + " NOT IN " +
                    "(" + String.join(",", Collections.nCopies(allies.size(), "?")) + ");";
        }

        PreparedStatement deleteSta = conn.prepareStatement(deleteSql);
        deleteSta.setInt(1, keyId);
        int i = 2;
        for (Object id : allies) {
            deleteSta.setInt(i, (Integer) id);
            i++;
        }
        deleteSta.executeUpdate();

        if (!allies.isEmpty()) {
            String insertSql = "INSERT IGNORE INTO " + tablePrefix + tableName + " (" + keyColumn + ", " + allyColumn + ")" +
                    " VALUES (?,?);";
            PreparedStatement insertSta = conn.prepareStatement(insertSql);
            for (IdObject id : allies) {
                insertSta.setInt(1, keyId);
                deleteSta.setInt(i, (Integer) id.value());
                insertSta.addBatch();
            }
            insertSta.executeUpdate();
        }
    }

    // ------ Utility methods ----- //

    private Map<String, Object> getValues(Map<String, ?> serialized) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : serialized.entrySet()) {
            if (!(entry.getValue() instanceof Collection)) {
                if (entry.getValue() instanceof ConfigurationSerializable) {
                    for (Map.Entry<String, Object> serEntry : getValues(((ConfigurationSerializable) entry.getValue()).serialize()).entrySet()) {
                        if (entry.getKey().isEmpty()) {
                            values.put(serEntry.getKey(), serEntry.getValue());
                        } else {
                            values.put(entry.getKey() + "_" + serEntry.getKey(), serEntry.getValue());
                        }
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
            String columnName = metaData.getColumnName(i);
            if (columnName.contains(".")) {
                columnName = columnName.substring(columnName.lastIndexOf('.'));
            }
            map.put(columnName, rs.getObject(i));
        }
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
}
