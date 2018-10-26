package me.crz.minecraft.kingdoms.utils;

/**
 * An object that can be used as a config key, usually an enum
 */
public interface ConfigKey {
    String name();

    default String getKey() {
        return name().toLowerCase().replace('_', '-');
    }

    static String toEnumString(String key) {
        return key.toUpperCase().replace('-', '_');
    }
}
