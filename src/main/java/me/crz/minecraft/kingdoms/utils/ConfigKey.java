package me.crz.minecraft.kingdoms.utils;

import java.util.Locale;

/**
 * An object that can be used as a config key, usually an enum
 */
public interface ConfigKey {

    /**
     * The name to use for the key
     * @return The name of the key
     */
    String name();

    default String getKey() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    static String getKey(Enum e) {
        return e.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    static String toEnumString(String key) {
        return key.toUpperCase(Locale.ROOT).replace('-', '_');
    }
}
