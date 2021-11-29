package de.themoep.settlements.perk;

import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementState;
import org.bukkit.configuration.ConfigurationSection;

public class LevelPerk extends Perk {
    private int level;
    private SettlementState state = SettlementState.VILLAGE;

    public LevelPerk(ConfigurationSection config) throws IllegalArgumentException {
        super(config);
        if (config.isInt("level")) {
            this.level = config.getInt("level");
        } else {
            throw new IllegalArgumentException("Config does not contain level!");
        }
        this.state = SettlementState.valueOf(config.getString("state", state.name()).toUpperCase());
    }

    public SettlementState getState() {
        return state;
    }

    public int getLevel() {
        return level;
    }

    public boolean matchesLevelRequirement(Settlement settlement) {
        return settlement.getState().above(getState()) || (settlement.getState() == getState() && settlement.getLevel() >= getLevel());
    }
}
