package de.themoep.settlements.perk;

import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementState;
import org.bukkit.configuration.ConfigurationSection;

public class SettlementPerk extends LevelPerk<Settlement> {
    private SettlementState state = SettlementState.VILLAGE;

    public SettlementPerk(ConfigurationSection config) throws IllegalArgumentException {
        super(config);
        this.state = SettlementState.valueOf(config.getString("state", state.name()).toUpperCase());
    }

    public SettlementState getState() {
        return state;
    }

    public boolean matchesLevelRequirement(Settlement settlement) {
        return settlement.getState().above(getState()) || (settlement.getState() == getState() && super.matchesLevelRequirement(settlement));
    }
}
