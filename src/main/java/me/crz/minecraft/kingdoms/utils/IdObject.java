package me.crz.minecraft.kingdoms.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

public class IdObject {
    private final Object value;

    private IdObject(Object value) {
        this.value = value;
    }

    /**
     * Utility method to build a IdObject object
     * @return never null, IdObject value might be null, check with {@link #isNull()}
     */
    public static IdObject get(Object id) {
        if (id instanceof ResultSet)
            try {
                return new IdObject(((ResultSet) id).getInt(1));
            } catch (SQLException e) {
                e.printStackTrace();
                return new IdObject(null);
            }
        if (id instanceof Map)
            if (((Map) id).containsKey("id"))
                return new IdObject(((Map) id).get("id"));
            else
                return new IdObject(((Map) id).get("_id"));
        return new IdObject(id);
    }

    public Object value() {
        return value;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        return this == o || (o instanceof IdObject && Objects.equals(((IdObject) o).value, value));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public boolean isNull() {
        return value == null;
    }
}
