package de.themoep.settlements.utils.config;

/**
 * @author Acrobot
 */
public interface TypelessConfigParser extends ConfigParser {

    default <T> Object toJava(Class<T> type, Object object) {
        return toJava(object);
    }

    /**
     * Parses a YAML "object" to Java-compatible object
     *
     * @param object Object to parse
     * @return Java-compatible object
     */
    Object toJava(Object object);

    default Object toYaml(Object object) {
        return object;
    }
}
