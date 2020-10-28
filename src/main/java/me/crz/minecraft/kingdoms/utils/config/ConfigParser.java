package me.crz.minecraft.kingdoms.utils.config;

/**
 * @author Acrobot
 */
public interface ConfigParser {

    /**
     * Parses a YAML "object" to Java-compatible object
     *
     * @param type The type of the returned object
     * @param object Object to parse
     * @return Java-compatible object
     */
    <T> Object toJava(Class<T> type, Object object);

    /**
     * Parses an object to a YAML-usable object
     *
     * @param object Object to parse
     * @return YAML object
     */
    Object toYaml(Object object);
}
