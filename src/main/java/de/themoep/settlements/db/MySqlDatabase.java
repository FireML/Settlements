package de.themoep.settlements.db;

import com.zaxxer.hikari.HikariDataSource;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import uk.firedev.poleislib.Loggers;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MySqlDatabase extends SqlDatabaseManager {
    private HikariDataSource ds;

    @Override
    public void connect() throws Exception {
        ds = new HikariDataSource();
        initConnectionClass(ds);

        String url = "jdbc:mysql://" + Config.Storage.HOST + ":" + Config.Storage.PORT + "/" + Config.Storage.DATABASE + Config.Storage.URL_PARAMETERS;
        if (ds.getDataSourceClassName() != null) {
            ds.addDataSourceProperty("url", url);
        } else {
            ds.setJdbcUrl(url);
        }
        ds.setUsername(Config.Storage.USER);
        ds.setPassword(Config.Storage.PASSWORD);
        ds.setConnectionTimeout(Config.Storage.CONNECTION_TIMEOUT);

        super.connect();

        addColumn(MySqlTable.RESIDENTS, "socialSpy", "BOOLEAN NOT NULL DEFAULT false AFTER swear");
        alterColumnOrRecreate(MySqlTable.CLAIMS, "buildState", "VARCHAR(255)");
        addColumn(MySqlTable.EMPIRES, "level", "INTEGER NOT NULL DEFAULT 1 AFTER capitalId");
        addColumn(MySqlTable.EMPIRES, "finishedMissions", "BIGINT NOT NULL DEFAULT 0 AFTER taxDebtAction");
        removeColumn(MySqlTable.EMPIRES, "allyClaim");
        addColumn(MySqlTable.EMPIRES, "allowAllyClaim", "BOOLEAN NOT NULL AFTER buildState");
        addColumn(MySqlTable.SETTLEMENTS, "missionPoints", "BIGINT NOT NULL DEFAULT 0 AFTER taxDebtAction");
        alterColumnOrRecreate(MySqlTable.EMPIRE_UNBANISHED, "unbanished_unbanishdate", "BIGINT(11)");
        alterColumnOrRecreate(MySqlTable.SETTLEMENT_UNBANISHED, "unbanished_unbanishdate", "BIGINT(11)");
        alterOrRecreate(MySqlTable.EMPIRE_UNBANISHED, "ADD CONSTRAINT list UNIQUE (empireId, unbanished_id, unbanished_date)");
        alterOrRecreate(MySqlTable.SETTLEMENT_UNBANISHED, "ADD CONSTRAINT list UNIQUE (settlementId, unbanished_id, unbanished_date)");
    }

    @Override
    protected Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while trying to get database connection", e);
        }
        return null;
    }

    @Override
    protected void closeConnection() {
        ds.close();
    }

    @Override
    protected void close(Connection conn) {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while trying to close a MySQL connection", e);
        }
    }

    @Override
    protected void initializeTables() throws SQLException {
        try (Connection conn = getConnection(); Statement sta = conn.createStatement()) {
            sta.addBatch("SET FOREIGN_KEY_CHECKS = 0");

            for (MySqlTable table : MySqlTable.values()) {
                sta.addBatch(table.getSql(tablePrefix));
            }

            sta.addBatch("SET FOREIGN_KEY_CHECKS = 1");

            sta.executeBatch();
        }
    }

    @Override
    protected String getInsertIgnore() {
        return "INSERT IGNORE";
    }

    @Override
    protected PreparedStatement executeSerializableInsert(Connection conn, String tableName, DatabaseSerializable serializable, Map<String, Object> values) throws SQLException {
        String sql = "INSERT INTO " + tablePrefix + tableName + " "
                + "(`" + String.join("`,`", values.keySet()) + "`)"
                + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")"
                + " ON DUPLICATE KEY UPDATE " + values.keySet().stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
        PreparedStatement sta = conn.prepareStatement(sql, serializable.getId().isNull() ? PreparedStatement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);

        int i = 0;
        for (Object value : values.values()) {
            sta.setObject(++i, toDbType(value));
        }

        sta.executeUpdate();
        return sta;
    }

    @Override
    protected void executeDataMapUpdate(Connection conn, String tableName, String keyColumn, int keyId, String mapKey, Map<String, ?> values, Set<String> columns, String... mapValueColumns) throws SQLException {
        String insertSql = "INSERT INTO " + tablePrefix + tableName + " (`" + keyColumn + "`,`" + mapKey + "`," + columns.stream().map(c -> "`" + c + "`").collect(Collectors.joining(",")) + ")"
                + " VALUES (?,?," + String.join(",", Collections.nCopies(columns.size(), "?")) + ")"
                + " ON DUPLICATE KEY UPDATE " + columns.stream().map(c -> "`" + c + "`=VALUES(`" + c + "`)").collect(Collectors.joining(",")) + ";";
        PreparedStatement insertSta = conn.prepareStatement(insertSql);
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            if (entry.getValue() instanceof Collection collection) {
                for (Object o : collection) {
                    insertSta.setInt(1, keyId);
                    insertSta.setObject(2, toDbType(entry.getKey()));
                    insertSta.setObject(3, toDbType(o));
                    insertSta.addBatch();
                }
            } else if (entry.getValue() instanceof Map && mapValueColumns.length > 0) {
                for (Map.Entry<Object, Object> o : ((Map<Object, Object>) entry.getValue()).entrySet()) {
                    insertSta.setInt(1, keyId);
                    insertSta.setObject(2, toDbType(entry.getKey()));
                    insertSta.setObject(3, toDbType(o.getKey()));
                    insertSta.setObject(4, toDbType(o.getValue()));
                    insertSta.addBatch();
                }
            } else {
                insertSta.setInt(1, keyId);
                insertSta.setObject(2, toDbType(entry.getKey()));

                int j = 3;
                Map<String, Object> map = null;
                if (entry.getValue() instanceof ConfigurationSerializable) {
                    map = ((ConfigurationSerializable) entry.getValue()).serialize();
                } else if (entry.getValue() instanceof Map) {
                    map = (Map<String, Object>) entry.getValue();
                }
                if (map != null) {
                    Map<String, Object> serMap = getValues(conn, map, mapKey);
                    for (String column : columns) {
                        Object o = serMap.get(column);
                        insertSta.setObject(j++, toDbType(o));
                    }
                } else {
                    insertSta.setObject(j, toDbType(entry.getValue()));
                }
                insertSta.addBatch();
            }
        }
        insertSta.executeBatch();
    }

    private void initConnectionClass(HikariDataSource ds) throws RuntimeException {
        String dataSourceClassName = tryDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        if (dataSourceClassName == null) {
            dataSourceClassName = tryDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        }
        if (dataSourceClassName != null) {
            SettlementsPlugin.getInstance().getLogger().log(Level.INFO, "Using " + dataSourceClassName + " database source");
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
                SettlementsPlugin.getInstance().getLogger().log(Level.INFO, "Using " + driverClassName + " database driver");
                ds.setDriverClassName(driverClassName);
            } else {
                throw new RuntimeException("Could not find database driver or data source class! Plugin wont work without a database!");
            }
        }
    }

    private String tryDriverClassName(String className) {
        try {
            Class.forName(className).getDeclaredConstructor().newInstance();
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

    private void alterOrRecreate(MySqlTable table, String alterSql) {
        String sql = "SELECT * FROM " + tablePrefix + table.getDbName() + " LIMIT 1";
        try (Connection conn = getConnection();
             Statement sta = conn.createStatement()) {
            ResultSet rs = sta.executeQuery(sql);
            if (rs.next()) {
                sta.execute("ALTER IGNORE TABLE `" + tablePrefix + table.getDbName() + "` " + alterSql);
            } else {
                sta.addBatch("SET FOREIGN_KEY_CHECKS = 0");
                sta.addBatch("DROP TABLE " + tablePrefix + table.getDbName());
                sta.addBatch(table.getSql(tablePrefix));
                sta.addBatch("SET FOREIGN_KEY_CHECKS = 1");
                sta.executeBatch();
            }
        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase(Locale.ROOT).contains("duplicate")) {
                plugin.getLogger().log(Level.SEVERE, "Error while trying to alter table " + table.getDbName(), e);
            }
        }
    }

    private void alterColumnOrRecreate(MySqlTable table, String column, String columnDefinition) {
        alterOrRecreate(table, "MODIFY COLUMN `" + column + "` " + columnDefinition + ";");
    }

    private void alterColumnOrRecreate(MySqlTable table, String oldColumn, String newColumn, String columnDefinition) {
        String sql = "SELECT * FROM " + tablePrefix + table.getDbName() + " LIMIT 1;";
        try (Connection conn = getConnection();
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
                sta.addBatch("DROP TABLE " + tablePrefix + table.getDbName());
                sta.addBatch(table.getSql(tablePrefix));
                sta.addBatch("SET FOREIGN_KEY_CHECKS = 1");
                sta.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error while trying to alter table " + table.getDbName(), e);
        }
    }

    protected void addColumn(MySqlTable table, String column, String definition) {
        try (Connection conn = getConnection();
             Statement sta = conn.createStatement()) {
            sta.execute("ALTER TABLE `" + tablePrefix + table.getDbName() + "` ADD COLUMN `" + column + "` " + definition + ";");
        } catch (SQLException ignored) {
            // column exists
        }
    }

    protected void removeColumn(MySqlTable table, String column) {
        try (Connection conn = getConnection();
             Statement sta = conn.createStatement()) {
            sta.execute("ALTER TABLE `" + tablePrefix + table.getDbName() + "` DROP COLUMN `" + column + "`;");
        } catch (SQLException ignored) {
            // column doesn't exist
        }
    }

    private void addTrigger(MySqlTable table, String triggerName, String triggerType, String definition) {
        String sql = "CREATE TRIGGER " + triggerName + " " + triggerType + " ON " + tablePrefix + table.getDbName() + " FOR EACH ROW " +
                definition + ";";
        try {
            Connection conn = getConnection();
            Statement sta = conn.createStatement();
            sta.execute(sql);
        } catch (SQLException e) {
            Loggers.logException(e, SettlementsPlugin.getInstance().getLogger());
        }
    }
}
