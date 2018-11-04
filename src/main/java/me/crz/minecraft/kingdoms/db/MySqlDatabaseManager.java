package me.crz.minecraft.kingdoms.db;

import com.zaxxer.hikari.HikariDataSource;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

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
import java.util.HashSet;
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
            sta.addBatch("SET FOREIGN_KEY_CHECKS = 0");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "empires ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "name VARCHAR(255) NOT NULL, INDEX (name), UNIQUE (name), "
                    + "capitalId INTEGER NOT NULL,"
                    + "FOREIGN KEY (capitalId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE RESTRICT,"
                    + "level INTEGER NOT NULL,"
                    + "description TEXT NOT NULL,"
                    + "symbol VARCHAR(16) NOT NULL,"
                    + "access VARCHAR(255) NOT NULL,"
                    + "buildState VARCHAR(255) NOT NULL,"
                    + "pvp BOOLEAN NOT NULL,"
                    + "fireSpread BOOLEAN NOT NULL,"
                    + "explosions BOOLEAN NOT NULL,"
                    + "mobSpawning BOOLEAN NOT NULL,"
                    + "buff BOOLEAN NOT NULL,"
                    + "bank BIGINT,"
                    + "joinFee BIGINT,"
                    + "tax BIGINT,"
                    + "bonusClaims INTEGER,"
                    + "homeLoc_world VARCHAR(255),"
                    + "homeLoc_x DOUBLE,"
                    + "homeLoc_y DOUBLE,"
                    + "homeLoc_z DOUBLE,"
                    + "homeLoc_yaw FLOAT,"
                    + "homeLoc_pitch FLOAT"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "empire_vaults ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "empireId INTEGER, INDEX(empireId),"
                    + "FOREIGN KEY (empireId) REFERENCES " + tablePrefix + "empires(id) ON DELETE CASCADE,"
                    + "content VARCHAR(255) NOT NULL,"
                    + "amount BIGINT,"
                    + "UNIQUE (empireId, content)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "empire_perks ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "empireId INTEGER, INDEX(empireId),"
                    + "FOREIGN KEY (empireId) REFERENCES " + tablePrefix + "empires(id) ON DELETE CASCADE,"
                    + "perk VARCHAR(255) NOT NULL,"
                    + "times INTEGER NOT NULL,"
                    + "lastUsed BIGINT,"
                    + "active BOOLEAN,"
                    + "UNIQUE (empireId, perk)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlements ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "name VARCHAR(255) NOT NULL, INDEX (name), UNIQUE (name), "
                    + "chiefId INTEGER NOT NULL,"
                    + "FOREIGN KEY (chiefId) REFERENCES " + tablePrefix + "residents(id) ON DELETE RESTRICT,"
                    + "level INTEGER NOT NULL,"
                    + "settlementState VARCHAR(255) NOT NULL,"
                    + "empireId INTEGER,"
                    + "FOREIGN KEY (empireId) REFERENCES " + tablePrefix + "empires(id) ON DELETE SET NULL,"
                    + "description TEXT NOT NULL,"
                    + "access VARCHAR(255) NOT NULL,"
                    + "buildState VARCHAR(255) NOT NULL,"
                    + "pvp BOOLEAN NOT NULL,"
                    + "fireSpread BOOLEAN NOT NULL,"
                    + "explosions BOOLEAN NOT NULL,"
                    + "mobSpawning BOOLEAN NOT NULL,"
                    + "buff BOOLEAN NOT NULL,"
                    + "bank BIGINT,"
                    + "joinFee BIGINT,"
                    + "tax BIGINT,"
                    + "bonusClaims INTEGER,"
                    + "homeLoc_world VARCHAR(255),"
                    + "homeLoc_x DOUBLE,"
                    + "homeLoc_y DOUBLE,"
                    + "homeLoc_z DOUBLE,"
                    + "homeLoc_yaw FLOAT,"
                    + "homeLoc_pitch FLOAT"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_outposts ("
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

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_vaults ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER, INDEX(settlementId),"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "content VARCHAR(255) NOT NULL,"
                    + "amount BIGINT,"
                    + "UNIQUE (settlementId, content)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_perks ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER, INDEX(settlementId),"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "perk VARCHAR(255) NOT NULL,"
                    + "times INTEGER NOT NULL,"
                    + "lastUsed BIGINT,"
                    + "active BOOLEAN,"
                    + "UNIQUE (settlementId, perk)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_uses ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER, INDEX(settlementId),"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "type VARCHAR(255) NOT NULL,"
                    + "state VARCHAR(255) NOT NULL,"
                    + "UNIQUE (settlementId, type)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_banished ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER, INDEX (settlementId),"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "banishedId INTEGER,"
                    + "FOREIGN KEY (banishedId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "UNIQUE (settlementId, banishedId)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "settlement_allies ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER, INDEX (settlementId),"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "allyId INTEGER,"
                    + "FOREIGN KEY (allyId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE CASCADE,"
                    + "UNIQUE (settlementId, allyId)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "residents ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "uuid CHAR(36) NOT NULL, UNIQUE (uuid),"
                    + "name VARCHAR(16) NOT NULL,"
                    + "settlementId INTEGER,"
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE SET NULL,"
                    + "settlementJoin BIGINT(11) NOT NULL,"
                    + "rank VARCHAR(255),"
                    + "empirerank VARCHAR(255),"
                    + "title INTEGER NOT NULL,"
                    + "bonusClaims INTEGER,"
                    + "taxOwed BIGINT NOT NULL,"
                    + "lastLogin BIGINT(11) NOT NULL,"
                    + "activeChannel VARCHAR(255) NOT NULL,"
                    + "swear BOOLEAN NOT NULL"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "resident_channels ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "userId INTEGER, INDEX (userId),"
                    + "FOREIGN KEY (userId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "channel VARCHAR(255),"
                    + "UNIQUE (userId, channel)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "resident_friends ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "userId INTEGER, INDEX (userId),"
                    + "FOREIGN KEY (userId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "friendId INTEGER,"
                    + "FOREIGN KEY (friendId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "UNIQUE (userId, friendId)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "resident_ignores ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "userId INTEGER, INDEX (userId),"
                    + "FOREIGN KEY (userId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "ignoredId INTEGER,"
                    + "FOREIGN KEY (ignoredId) REFERENCES " + tablePrefix + "residents(id) ON DELETE CASCADE,"
                    + "UNIQUE (userId, ignoredId)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "claims ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "settlementId INTEGER NOT NULL, INDEX (settlementId), "
                    + "FOREIGN KEY (settlementId) REFERENCES " + tablePrefix + "settlements(id) ON DELETE RESTRICT,"
                    + "world VARCHAR(255),INDEX(world),"
                    + "x INTEGER,INDEX(x),"
                    + "z INTEGER,INDEX(z),"
                    + "UNIQUE(world, x, z),"
                    + "unclaimIn INTEGER,"
                    + "ownerId INTEGER,"
                    + "FOREIGN KEY (ownerId) REFERENCES " + tablePrefix + "residents(id) ON DELETE SET NULL,"
                    + "price INTEGER DEFAULT 0,"
                    + "forSale BOOLEAN NOT NULL,"
                    + "embassy BOOLEAN NOT NULL,"
                    + "description TEXT,"
                    + "pvp BOOLEAN NOT NULL,"
                    + "fireSpread BOOLEAN NOT NULL,"
                    + "explosions BOOLEAN NOT NULL,"
                    + "mobSpawning BOOLEAN NOT NULL,"
                    + "buff BOOLEAN NOT NULL,"
                    + "buildState VARCHAR(255) NOT NULL,"
                    + "link VARCHAR(36)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "claim_uses ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "claimId INTEGER, INDEX(claimId),"
                    + "FOREIGN KEY (claimId) REFERENCES " + tablePrefix + "claims(id) ON DELETE CASCADE,"
                    + "type VARCHAR(255) NOT NULL,"
                    + "state VARCHAR(255) NOT NULL,"
                    + "UNIQUE (claimId, type)"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("CREATE TABLE IF NOT EXISTS " + tablePrefix + "logs ("
                    + "id INTEGER PRIMARY KEY AUTO_INCREMENT,"
                    + "empireId INTEGER, INDEX(empireId),"
                    + "settlementId INTEGER, INDEX(settlementId),"
                    + "residentId INTEGER, "
                    + "FOREIGN KEY (residentId) REFERENCES " + tablePrefix + "residents(id),"
                    + "`action` VARCHAR(255) NOT NULL,"
                    + "type VARCHAR(255) NOT NULL,"
                    + "amount BIGINT NOT NULL,"
                    + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ") DEFAULT CHARACTER SET=utf8 AUTO_INCREMENT=1;");

            sta.addBatch("SET FOREIGN_KEY_CHECKS = 1");

            sta.executeBatch();
        }
    }

    @Override
    public void saveEmpire(Empire empire) {
        Utils.catchSync();
        Map<String, Object> values = getValues(empire.serialize(), "senators", "vault", "perks");
        String sql = "INSERT INTO " + tablePrefix + "empires "
                + "(`" + String.join("`,`", values.keySet()) + "`)"
                + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")"
                + " ON DUPLICATE KEY UPDATE " + values.keySet().stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement(sql, empire.getId().isNull() ? PreparedStatement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
            int i = 0;
            for (Object value : values.values()) {
                i++;
                sta.setObject(i, value);
            }
            sta.executeUpdate();
            if (empire.getId().isNull()) {
                ResultSet rs = sta.getGeneratedKeys();
                if (rs.next()) {
                    empire.setId(IdObject.get(rs));
                }
            }
            if (!empire.getId().isNull()) {
                updateDataMap(conn, "empire_vaults", "empireId", (Integer) empire.getId().value(), "content", "amount", empire.getVault());
                updateDataMap(conn, "empire_perks", "empireId", (Integer) empire.getId().value(), "perk", "",
                        empire.getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while saving empire " + empire.getName() + " to database!", e);
        }
    }

    @Override
    public void deleteEmpire(Empire empire) {
        Utils.catchSync();
        if (empire.getId() != null) {
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "empires WHERE id=?")) {
                sta.setInt(1, (Integer) empire.getId().value());
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete empire " + empire.getId().value() + "/" + empire.getName() + " from database!", e);
            }
        } else {
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "empires WHERE name LIKE ? LIMIT 1")) {
                sta.setString(1, empire.getName());
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to delete empire " + empire.getId().value() + "/" + empire.getName() + " from database!", e);
            }
        }
    }

    @Override
    public void saveVillage(Village village) {
        Utils.catchSync();
        Map<String, Object> values = getValues(village.serialize(), "outposts", "banished", "uses", "vault", "perks");
        String sql = "INSERT INTO " + tablePrefix + "settlements "
                + "(`" + String.join("`,`", values.keySet()) + "`)"
                + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")"
                + " ON DUPLICATE KEY UPDATE " + values.keySet().stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement(sql, village.getId().isNull() ? PreparedStatement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
            int i = 0;
            for (Object value : values.values()) {
                i++;
                sta.setObject(i, value);
            }
            sta.executeUpdate();
            if (village.getId().isNull()) {
                ResultSet rs = sta.getGeneratedKeys();
                if (rs.next()) {
                    village.setId(IdObject.get(rs));
                }
            }
            if (!village.getId().isNull()) {
                updateUserList(conn, "settlement_banished", "settlementId", (Integer) village.getId().value(), "banishedId", village.getBanishedResidents());
                updateAllyList(conn, "settlement_allies", "settlementId", (Integer) village.getId().value(), "banishedId", village.getAllies());
                updateDataMap(conn, "settlement_vaults", "settlementId", (Integer) village.getId().value(), "content", "amount", village.getVault());
                updateDataMap(conn, "settlement_outposts", "settlementId", (Integer) village.getId().value(), "name", "", village.getOutpostHomes());
                updateDataMap(conn, "settlement_uses", "settlementId", (Integer) village.getId().value(), "type", "state",
                        village.getUseStates().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().name())));
                updateDataMap(conn, "settlement_perks", "settlementId", (Integer) village.getId().value(), "perk", "",
                        village.getPerksMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().serialize())));
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
        if (lands.length == 0) {
            return;
        }
        try (Connection conn = ds.getConnection();
            PreparedStatement sta = conn.prepareStatement("DELETE FROM " + tablePrefix + "claims WHERE world = ? AND x = ? AND z = ?;")) {
            for (OwnedLand land : lands) {
                sta.setString(1, land.getWorldName());
                sta.setInt(2, land.getX());
                sta.setInt(3, land.getZ());
                sta.addBatch();
            }
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
    public void saveLand(OwnedLand... lands) {
        Utils.catchSync();
        if (lands.length == 0) {
            return;
        }
        for (OwnedLand land : lands) {
            Map<String, Object> values = getValues(land.serialize(), "ownerVillage", "uses");
            String sql = "INSERT INTO " + tablePrefix + "claims "
                    + "(`" + String.join("`,`", values.keySet()) + "`)"
                    + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")"
                    + " ON DUPLICATE KEY UPDATE " + values.keySet().stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement(sql, land.getId() == null ? PreparedStatement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
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
                    updateDataMap(conn, "claim_uses", "claimId", (Integer) land.getId().value(), "type", "state",
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
        if (residents.length == 0) {
            return;
        }
        for (Resident resident : residents) {
            Map<String, Object> values = getValues(resident.serialize(), "channels", "ignores", "friends");
            String sql = "INSERT INTO " + tablePrefix + "residents "
                    + "(`" + String.join("`,`", values.keySet()) + "`)"
                    + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")"
                    + " ON DUPLICATE KEY UPDATE " + values.keySet().stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
            try (Connection conn = ds.getConnection();
                 PreparedStatement sta = conn.prepareStatement(sql, resident.getId() == null ? PreparedStatement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
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
                    updateChannelList(conn, "resident_channels", "userId", (Integer) resident.getId(), "channel", resident.getChannels());
                    updateUserList(conn, "resident_friends", "userId", (Integer) resident.getId(), "friendId", resident.getFriends());
                    updateUserList(conn, "resident_ignores", "userId", (Integer) resident.getId(), "ignoredId", resident.getIgnore());
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error while saving player " + resident.getName() + "/" + resident.getUUID() + " to database!", e);
            }
        }
    }

    @Override
    public void log(Empire empire, Village village, Resident resident, String action, Map<String, Long> values) {
        if (values.isEmpty() || village.getId().isNull()) {
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
                    sta.setInt(2, (Integer) village.getId().value());
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
        if (!resident.getVillageRank().atLeast(VillageRank.CHIEF)) {
            sql += " AND `l.action` = 'deposit'";
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
        if (!resident.getVillageRank().atLeast(VillageRank.CHIEF)) {
            sql += " AND `l.action` = 'deposit'";
        }
        sql += " ORDER BY l.id DESC LIMIT " + start + ",20;";
        try (Connection conn = ds.getConnection();
             PreparedStatement sta = conn.prepareStatement(sql)) {
            sta.setInt(1, (Integer) id.value());
            ResultSet rs = sta.executeQuery();
            while (rs.next()) {
                int amount = rs.getInt("amount");
                if (amount == 0) {
                    continue;
                }
                VillageRank rank = VillageRank.RESIDENT;
                try {
                    rank = VillageRank.valueOf(rs.getString("playerRank").toUpperCase());
                } catch (IllegalArgumentException ignored) {}

                Village village = null;
                int villageId = rs.getInt("empireId");
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
                        "name", rs.getString("playerName"),
                        "uuid", rs.getString("playerId"),
                        "rank", rank.getTag(village),
                        "title", rs.getString("playerTitle"),
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
            PreparedStatement sSta = conn.prepareStatement("SELECT uuid FROM " + tablePrefix + "residents WHERE senator = true AND settlementId = ?;");
            sSta.setInt(1, memberId);
            ResultSet sRs = sSta.executeQuery();
            while (sRs.next()) {
                senators.add(sRs.getString("uuid"));
            }
        }
        map.put("senators", senators);

        PreparedStatement vSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "empire_vaults WHERE settlementId = ?;");
        vSta.setInt(1, id);
        ResultSet vRs = vSta.executeQuery();
        Map<String, Long> vault = new LinkedHashMap<>();
        while (vRs.next()) {
            vault.put(vRs.getString("content"), vRs.getLong("amount"));
        }
        map.put("vault", vault);

        PreparedStatement pSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "empire_perks WHERE settlementId = ?;");
        pSta.setInt(1, id);
        ResultSet pRs = pSta.executeQuery();
        Map<String, Map<String, Object>> unlockedPerks = new LinkedHashMap<>();
        while (pRs.next()) {
            unlockedPerks.put(pRs.getString("perk"), toMap(pRs));
        }
        map.put("perks", unlockedPerks);

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

        map.put("allies", selectList(conn, "settlement_allies", "settlementId", rs.getInt("id"), "allyId"));

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
            use.put(uRs.getString("type"), uRs.getString("state"));
        }
        map.put("uses", use);

        PreparedStatement vSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_vaults WHERE settlementId = ?;");
        vSta.setInt(1, id);
        ResultSet vRs = vSta.executeQuery();
        Map<String, Long> vault = new LinkedHashMap<>();
        while (vRs.next()) {
            vault.put(vRs.getString("content"), vRs.getLong("amount"));
        }
        map.put("vault", vault);

        PreparedStatement pSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "settlement_perks WHERE settlementId = ?;");
        pSta.setInt(1, id);
        ResultSet pRs = pSta.executeQuery();
        Map<String, Map<String, Object>> unlockedPerks = new LinkedHashMap<>();
        while (pRs.next()) {
            unlockedPerks.put(pRs.getString("perk"), toMap(pRs));
        }
        map.put("perks", unlockedPerks);

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

        PreparedStatement uSta = conn.prepareStatement("SELECT * FROM " + tablePrefix + "claim_uses WHERE claimId = ?;");
        uSta.setInt(1, id);
        ResultSet uRs = uSta.executeQuery();
        Map<String, String> use = new LinkedHashMap<>();
        while (uRs.next()) {
            use.put(uRs.getString("type"), uRs.getString("state"));
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
                for (String column : getValues(((ConfigurationSerializable) value).serialize(), mapKey).keySet()) {
                    if (mapValue.isEmpty()) {
                        columns.add(column);
                    } else {
                        columns.add(mapValue + "_" + column);
                    }
                }
            } else if (value instanceof Map) {
                if (((Map) value).keySet().iterator().next() instanceof String) {
                    for (String column : getValues((Map<String, ?>) value, mapKey).keySet()) {
                        if (mapValue.isEmpty()) {
                            columns.add(column);
                        } else {
                            columns.add(mapValue + "_" + column);
                        }
                    }
                }
            } else if (!mapValue.isEmpty()) {
                columns.add(mapValue);
            } else {
                return;
            }
            String insertSql = "INSERT IGNORE INTO " + tablePrefix + tableName + " (" + keyColumn + ", " + mapKey + ", " + String.join(",", columns) + ")" +
                    " VALUES (?,?," + String.join(",", Collections.nCopies(columns.size(), "?")) + ")" +
                    " ON DUPLICATE KEY UPDATE " + columns.stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
            PreparedStatement insertSta = conn.prepareStatement(insertSql);
            for (Map.Entry<String, ?> entry : values.entrySet()) {
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
            insertSta.executeBatch();
        }
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
        for (IdObject id : allies) {
            deleteSta.setInt(i, (Integer) id.value());
            i++;
        }
        deleteSta.executeUpdate();

        if (!allies.isEmpty()) {
            String insertSql = "INSERT IGNORE INTO " + tablePrefix + tableName + " (" + keyColumn + ", " + allyColumn + ")" +
                    " VALUES (?,?);";
            PreparedStatement insertSta = conn.prepareStatement(insertSql);
            for (IdObject id : allies) {
                insertSta.setInt(1, keyId);
                insertSta.setInt(2, (Integer) id.value());
                insertSta.addBatch();
            }
            insertSta.executeBatch();
        }
    }

    private void updateChannelList(Connection conn, String tableName, String keyColumn, int keyId, String channelColumn, Collection<ChatChannel> channels) throws SQLException {
        String deleteSql;
        if (channels.isEmpty()) {
            deleteSql = "DELETE FROM " + tablePrefix + tableName + " " +
                    " WHERE " + keyColumn + " = ?;";
        } else {
            deleteSql = "DELETE t FROM " + tablePrefix + tableName + " AS t" +
                    " WHERE t." + keyColumn + " = ?" +
                    " AND t." + channelColumn + " NOT IN " +
                    "(" + String.join(",", Collections.nCopies(channels.size(), "?")) + ");";
        }

        PreparedStatement deleteSta = conn.prepareStatement(deleteSql);
        deleteSta.setInt(1, keyId);
        int i = 2;
        for (ChatChannel channel : channels) {
            deleteSta.setString(i, channel.name());
            i++;
        }
        deleteSta.executeUpdate();

        if (!channels.isEmpty()) {
            String insertSql = "INSERT IGNORE INTO " + tablePrefix + tableName + " (" + keyColumn + ", " + channelColumn + ")" +
                    " VALUES (?,?);";
            PreparedStatement insertSta = conn.prepareStatement(insertSql);
            for (ChatChannel channel : channels) {
                insertSta.setInt(1, keyId);
                insertSta.setString(2, channel.name());
                insertSta.addBatch();
            }
            insertSta.executeBatch();
        }
    }

    // ------ Utility methods ----- //

    private <K> Map<K, Object> getValues(Map<K, ?> serialized, K... ignoredKeys) {
        Set<K> ignored = new HashSet<>(Arrays.asList(ignoredKeys));
        Map<K, Object> values = new LinkedHashMap<>();
        for (Map.Entry<K, ?> entry : serialized.entrySet()) {
            if (!ignored.contains(entry.getKey()) && !(entry.getValue() instanceof Collection)) {
                if (entry.getValue() instanceof Map) {
                    for (Map.Entry<K, Object> serEntry : getValues((Map<K, Object>) entry.getValue(), ignoredKeys).entrySet()) {
                        if (entry.getKey() instanceof String && !((String) entry.getKey()).isEmpty()) {
                            values.put((K) (entry.getKey() + "_" + serEntry.getKey()), serEntry.getValue());
                        } else {
                            values.put(serEntry.getKey(), serEntry.getValue());
                        }
                    }
                } else if (entry.getValue() instanceof UUID) {
                    Object id = null;
                    if (!entry.getValue().equals(new UUID(0, 0))) {
                        Resident resident = ResidentManager.getInstance().getResident((UUID) entry.getValue());
                        id = resident != null ? resident.getId() : null;
                    }
                    if (entry.getKey() instanceof String && !((String) entry.getKey()).endsWith("Id")) {
                        values.put((K) (entry.getKey() + "Id"), id);
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
