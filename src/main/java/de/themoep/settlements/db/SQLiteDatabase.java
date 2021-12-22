package de.themoep.settlements.db;

import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SQLiteDatabase extends SqlDatabaseManager {
    private final Object connectionLock = new Object();

    private Connection connection;

    @Override
    public void connect() throws Exception {
        createConnection();
        super.connect();
    }

    @Override
    protected Connection getConnection() {
        try {
            synchronized (connectionLock) {
                if (connection == null || !connection.isValid(1)) {
                    createConnection();
                }
                return connection;
            }
        } catch (SQLException e) {
            SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Error while trying to get database connction", e);
        }
        return null;
    }

    private void createConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(SettlementsPlugin.getInstance().getDataFolder(), "data.db"));
        connection.createStatement().execute("PRAGMA FOREIGN_KEYS = ON;");
    }

    @Override
    protected void closeConnection() throws SQLException {
        connection.close();
    }

    @Override
    protected void close(Connection conn) {
        // Do not close our connection!
    }

    @Override
    protected void initializeTables() throws SQLException {
        Connection conn = getConnection();
        try (Statement sta = conn.createStatement()) {
            sta.addBatch("PRAGMA FOREIGN_KEY_CHECKS = OFF");

            for (SQLiteTable table : SQLiteTable.values()) {
                sta.addBatch(table.getSql(tablePrefix));
            }

            sta.addBatch("PRAGMA FOREIGN_KEY_CHECKS = ON");

            sta.executeBatch();
        }
    }

    @Override
    protected String getInsertIgnore() {
        return "INSERT OR IGNORE";
    }

    @Override
    protected PreparedStatement executeSerializableInsert(Connection conn, String tableName, DatabaseSerializable serializable, Map<String, Object> values) throws SQLException {
        if (!serializable.getId().isNull()) {
            String updateSql = "UPDATE " + tablePrefix + tableName + " SET " + values.keySet().stream().map(c -> "`" + c + "`" + "=?").collect(Collectors.joining(","))
                    + " WHERE id=?;";
            PreparedStatement updateSta = conn.prepareStatement(updateSql);
            int i = 0;
            for (Object value : values.values()) {
                updateSta.setObject(++i, value);
            }
            updateSta.setInt(++i, (Integer) serializable.getId().value());
            updateSta.executeUpdate();
        }

        String insertSql = getInsertIgnore() + " INTO " + tablePrefix + tableName
                + " (`" + String.join("`,`", values.keySet()) + "`)"
                + " VALUES (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")";
        PreparedStatement insertSta = conn.prepareStatement(insertSql, serializable.getId().isNull() ? PreparedStatement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
        int i = 0;
        for (Object value : values.values()) {
            insertSta.setObject(++i, toDbType(value));
        }
        insertSta.executeUpdate();

        return insertSta;
    }

    @Override
    protected void executeDataMapUpdate(Connection conn, String tableName, String keyColumn, int keyId, String mapKey, Map<String, ?> values, Set<String> columns, String... mapValueColumns) throws SQLException {
        PreparedStatement updateSta = conn.prepareStatement("UPDATE " + tablePrefix + tableName + " SET " + columns.stream().map(c -> "`" + c + "`" + "=?").collect(Collectors.joining(","))
                + " WHERE `" + keyColumn + "`=? AND `" + mapKey + "`=?;");
        PreparedStatement insertSta = conn.prepareStatement(getInsertIgnore() + " INTO " + tablePrefix + tableName + " (`" + keyColumn + "`,`" + mapKey + "`," + columns.stream().map(c -> "`" + c + "`").collect(Collectors.joining(",")) + ")"
                + " VALUES (?,?," + String.join(",", Collections.nCopies(columns.size(), "?")) + ");");
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            if (entry.getValue() instanceof Collection) {
                for (Object o : (Collection<Object>) entry.getValue()) {
                    updateSta.setObject(1, toDbType(o));
                    updateSta.setObject(2, toDbType(entry.getKey()));
                    updateSta.setInt(3, keyId);
                    updateSta.addBatch();

                    insertSta.setInt(1, keyId);
                    insertSta.setObject(2, toDbType(entry.getKey()));
                    insertSta.setObject(3, toDbType(o));
                    insertSta.addBatch();
                }
            } else if (entry.getValue() instanceof Map && mapValueColumns.length > 0) {
                for (Map.Entry<Object, Object> o : ((Map<Object, Object>) entry.getValue()).entrySet()) {
                    updateSta.setObject(1, toDbType(o.getKey()));
                    updateSta.setObject(2, toDbType(o.getValue()));
                    updateSta.setObject(3, toDbType(entry.getKey()));
                    updateSta.setInt(4, keyId);
                    updateSta.addBatch();

                    insertSta.setInt(1, keyId);
                    insertSta.setObject(2, toDbType(entry.getKey()));
                    insertSta.setObject(3, toDbType(o.getKey()));
                    insertSta.setObject(4, toDbType(o.getValue()));
                    insertSta.addBatch();
                }
            } else {
                Map<String, Object> map = null;
                if (entry.getValue() instanceof ConfigurationSerializable) {
                    map = ((ConfigurationSerializable) entry.getValue()).serialize();
                } else if (entry.getValue() instanceof Map) {
                    map = (Map<String, Object>) entry.getValue();
                }
                if (map != null) {
                    Map<String, Object> serMap = getValues(conn, map, mapKey);

                    updateSta.setInt(columns.size() + 1, keyId);
                    updateSta.setObject(columns.size() + 2, toDbType(entry.getKey()));

                    insertSta.setInt(1, keyId);
                    insertSta.setObject(2, toDbType(entry.getKey()));

                    int j = 0;
                    for (String column : columns) {
                        j++;
                        updateSta.setObject(j, toDbType(serMap.get(column)));
                        insertSta.setObject(2 + j, toDbType(serMap.get(column)));
                    }
                } else {
                    updateSta.setObject(1, toDbType(entry.getValue()));
                    updateSta.setInt(2, keyId);
                    updateSta.setObject(3, toDbType(entry.getKey()));

                    insertSta.setInt(1, keyId);
                    insertSta.setObject(2, toDbType(entry.getKey()));
                    insertSta.setObject(3, toDbType(entry.getValue()));
                }
                updateSta.addBatch();
                insertSta.addBatch();
            }
        }
        updateSta.executeBatch();
        insertSta.executeBatch();
    }
}
