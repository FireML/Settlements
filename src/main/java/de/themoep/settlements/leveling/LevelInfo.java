package de.themoep.settlements.leveling;

import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public abstract class LevelInfo {
    private final int level;
    private final LevelInfo previous;
    private final Map<String, Long> requirements = new LinkedHashMap<>();


    public LevelInfo(int level, LevelInfo previous, ConfigurationSection config) {
        this.level = level;
        this.previous = previous;
        ConfigurationSection requirementsConfig = null;
        if (config != null) {
            requirementsConfig = config.getConfigurationSection("requirements");
        }
        if (requirementsConfig != null) {
            for (String key : requirementsConfig.getKeys(false)) {
                long value = requirementsConfig.getLong(key, -1);
                if (value > 0) {
                    requirements.put(key.toUpperCase(Locale.ROOT), value);
                }
            }
        }
    }

    public int getLevel() {
        return level;
    }

    public Map<String, Long> getRequirements() {
        return requirements;
    }

    public LevelInfo getPrevious() {
        return previous;
    }
}
