package me.crz.minecraft.kingdoms.perk;

import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.configuration.ConfigurationSection;

public class VillagePerk extends Perk {
    private VillageState state = VillageState.VILLAGE;

    public VillagePerk(ConfigurationSection config) throws IllegalArgumentException {
        super(config);
        this.state = VillageState.valueOf(config.getString("state", state.name()).toUpperCase());
    }

    public VillageState getState() {
        return state;
    }
}
