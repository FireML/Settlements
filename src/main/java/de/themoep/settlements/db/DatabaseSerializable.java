package de.themoep.settlements.db;

import de.themoep.settlements.utils.ConfigKey;
import de.themoep.settlements.utils.IdObject;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.LinkedHashMap;

public interface DatabaseSerializable extends ConfigurationSerializable, ConfigKey {

    IdObject getId();

    void setId(IdObject id);

    default String getDbName() {
        return getKey();
    }

    void save();

    LinkedHashMap<String, Object> serialize();
}
