package me.crz.minecraft.kingdoms.perk;

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
    protected PerkEffect.EffectLocation getDefaultEffectLocation() {
        return PerkEffect.EffectLocation.EMPIRE_WORLD;
    }

    @Override
    public boolean hasEffects() {
        return super.hasEffects() || isFlying();
    }
}
