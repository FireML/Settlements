package de.themoep.settlements.perk;

import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageState;
import org.bukkit.configuration.ConfigurationSection;

public class LevelPerk extends Perk {
    private int level;
    private VillageState state = VillageState.VILLAGE;

    public LevelPerk(ConfigurationSection config) throws IllegalArgumentException {
        super(config);
        if (config.isInt("level")) {
            this.level = config.getInt("level");
        } else {
            throw new IllegalArgumentException("Config does not contain level!");
        }
        this.state = VillageState.valueOf(config.getString("state", state.name()).toUpperCase());
    }

    public VillageState getState() {
        return state;
    }

    public int getLevel() {
        return level;
    }

    public boolean matchesLevelRequirement(Village village) {
        return village.getState().above(getState()) || (village.getState() == getState() && village.getLevel() >= getLevel());
    }
}
