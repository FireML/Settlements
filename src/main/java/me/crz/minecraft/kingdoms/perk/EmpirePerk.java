package me.crz.minecraft.kingdoms.perk;

import me.crz.minecraft.kingdoms.empire.Empire;
import org.bukkit.configuration.ConfigurationSection;

public class EmpirePerk extends Perk {
    private boolean flying = false;
    private int bonusRadius = 0;

    public EmpirePerk(ConfigurationSection config) throws IllegalArgumentException {
        super(config);
        this.flying = config.getBoolean("world.flying", flying);
        this.bonusRadius = config.getInt("world.radius", bonusRadius);
    }

    public boolean isFlying() {
        return flying;
    }

    public int getBonusRadius() {
        return bonusRadius;
    }

    @Override
    public boolean hasEffects() {
        return super.hasEffects() || isFlying();
    }
}
