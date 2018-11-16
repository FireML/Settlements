package me.crz.minecraft.kingdoms.db;

import me.crz.minecraft.kingdoms.utils.IdObject;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface DatabaseSerializable extends ConfigurationSerializable {

    IdObject getId();

    void setId(IdObject id);

    default String getDbName() {
        return getClass().getSimpleName().toLowerCase();
    };

    String getDbDescription();
}
