package de.themoep.settlements.perk;

import de.themoep.settlements.leveling.Levelable;
import org.bukkit.configuration.ConfigurationSection;

public class LevelPerk<L extends Levelable<?>> extends Perk {
    private final int level;

    public LevelPerk(ConfigurationSection config) throws IllegalArgumentException {
        super(config);
        if (config.isInt("level")) {
            this.level = config.getInt("level");
        } else {
            throw new IllegalArgumentException("Config does not contain level!");
        }
    }

    public int getLevel() {
        return level;
    }

    public boolean matchesLevelRequirement(L levelable) {
        return levelable.getLevel() >= level;
    }
}
