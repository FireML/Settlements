package me.crz.minecraft.kingdoms.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class IdObject {
    private final Object value;

    private IdObject(Object value) {
        this.value = value;
    }

    /**
     * Utility method to build a IdObject object
     * @return null if id is null or an error occured
     */
    public static IdObject get(Object id) {
        if (id == null)
            return null;
        if (id instanceof ResultSet)
            try {
                return new IdObject(((ResultSet) id).getInt(1));
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
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
        return this == o || (o instanceof IdObject && o.hashCode() == hashCode());
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public boolean isNull() {
        return value == null;
    }
}
