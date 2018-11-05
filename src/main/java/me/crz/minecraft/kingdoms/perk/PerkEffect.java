package me.crz.minecraft.kingdoms.perk;

import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.apache.commons.lang.Validate;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

public class PerkEffect {
    private PotionEffectType type;
    private int strength = 1;
    private int length = 0;
    private boolean ambient = true;
    private boolean particles = false;
    private boolean icon = true;

    private EffectLocation applyIn = EffectLocation.OWN_SETTLEMENT;
    private EffectInfluenced applyTo = EffectInfluenced.RESIDENT;

    public PerkEffect(PotionEffectType type) {
        Validate.notNull(type, "Unknown type!");
        this.type = type;
    }

    public PerkEffect(Map<String, Object> map) {
        this(PotionEffectType.getByName((String) map.get("type")));
        strength = (int) map.getOrDefault("strength", strength);
        if (strength <= 0) {
            strength = 1;
        }
        length = (int) map.getOrDefault("length", length);
        ambient = (boolean) map.getOrDefault("ambient", ambient);
        particles = (boolean) map.getOrDefault("particles", particles);
        icon = (boolean) map.getOrDefault("icon", icon);
        applyIn = EffectLocation.valueOf((String) map.getOrDefault("apply.in", applyIn.name()));
        applyTo = EffectInfluenced.valueOf((String) map.getOrDefault("apply.to", applyTo.name()));
    }

    public PotionEffectType getType() {
        return type;
    }

    public int getStrength() {
        return strength;
    }

    public int getLength() {
        return length;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public boolean isParticles() {
        return particles;
    }

    public boolean isIcon() {
        return icon;
    }

    public EffectLocation getApplyIn() {
        return applyIn;
    }

    public EffectInfluenced getApplyTo() {
        return applyTo;
    }

    public PotionEffect getPotionEffect() {
        return new PotionEffect(type, length > 0 ? length * 20: 300, strength - 1, ambient, particles, icon);
    }

    public enum EffectLocation implements NameTag {
        PLOT,
        PLOT_FRIENDS,
        OWN_SETTLEMENT,
        TRUSTED_SETTLEMENT,
        ALLIED_SETTLEMENT,
        ENEMY_SETTLEMENT,
        UNCLAIMED,
        EMPIRE_WORLD,
        EVERYWHERE
    }

    public enum EffectInfluenced implements NameTag {
        RESIDENT(VillageRank.RESIDENT),
        RECRUITER(VillageRank.RECRUITER),
        BUILDER(VillageRank.BUILDER),
        ELDER(VillageRank.ELDER),
        CHIEF(VillageRank.CHIEF),
        TRUSTED(null),
        ALLIES(null),
        ENEMIES(null),
        FREE(null);

        private final VillageRank rank;

        EffectInfluenced(VillageRank rank) {
            this.rank = rank;
        }

        public VillageRank getRank() {
            return rank;
        }
    }
}
