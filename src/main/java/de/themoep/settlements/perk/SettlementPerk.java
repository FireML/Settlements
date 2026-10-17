package de.themoep.settlements.perk;

import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementState;
import org.bukkit.configuration.ConfigurationSection;

public class SettlementPerk extends Perk {
    private final int level;
    private SettlementState state = SettlementState.VILLAGE;

    public SettlementPerk(ConfigurationSection config) throws IllegalArgumentException {
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

    public boolean matchesLevelRequirement(Settlement settlement) {
        return settlement.getState().above(getState()) || (settlement.getState() == getState() && settlement.getLevel() >= getLevel());
    }

    public int getLevel() {
        return level;
    }
}
