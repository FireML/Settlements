package me.crz.minecraft.kingdoms.perk;

import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.ConfigKey;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class PerkEffect {
    private PotionEffectType type;
    private int strength = 1;
    private int length = 0;
    private boolean ambient = true;
    private boolean particles = false;
    private boolean icon = true;

    private EffectLocation applyIn;
    private EffectInfluenced applyTo = EffectInfluenced.MEMBERS;

    public PerkEffect(PotionEffectType type) {
        Validate.notNull(type, "Unknown type!");
        this.type = type;
    }

    public PerkEffect(Map<String, Object> map, EffectLocation applyIn) {
        this(PotionEffectType.getByName((String) map.get("type")));
        this.strength = (int) map.getOrDefault("strength", strength);
        if (this.strength <= 0) {
            this.strength = 1;
        }
        this.length = (int) map.getOrDefault("length", length);
        this.ambient = (boolean) map.getOrDefault("ambient", ambient);
        this.particles = (boolean) map.getOrDefault("particles", particles);
        this.icon = (boolean) map.getOrDefault("icon", icon);
        this.applyIn = EffectLocation.valueOf(ConfigKey.toEnumString(Utils.getOrDefault(map, "apply.in", applyIn.name())));
        this.applyTo = EffectInfluenced.valueOf(ConfigKey.toEnumString(Utils.getOrDefault(map, "apply.to", applyTo.name())));
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

    public boolean isPermanent() {
        return length <= 0;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public boolean hasParticles() {
        return particles;
    }

    public boolean hasIcon() {
        return icon;
    }

    public EffectLocation getApplyIn() {
        return applyIn;
    }

    public EffectInfluenced getApplyTo() {
        return applyTo;
    }

    public PotionEffect getPotionEffect() {
        return new PotionEffect(getType(), isPermanent() ? 1200 : getLength() * 20, getStrength() - 1, isAmbient(), hasParticles(), hasIcon());
    }

    public String getIdentifier() {
        return getType().getName() + "|" + (getStrength() - 1) + "|" + isAmbient() + "|" + hasParticles();
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

    public static class EffectInfluenced implements NameTag {
        private final String name;

        private static final Map<String, EffectInfluenced> byName = new HashMap<>();

        public static final EffectInfluenced TRUSTED = new EffectInfluenced("trusted");
        public static final EffectInfluenced ALLIES = new EffectInfluenced("allies");
        public static final EffectInfluenced ENEMIES = new EffectInfluenced("enemies");
        public static final EffectInfluenced MEMBERS = new EffectInfluenced("members");
        public static final EffectInfluenced FREE = new EffectInfluenced("free");
        public static final EffectInfluenced ALL = new EffectInfluenced("all");

        public EffectInfluenced(String name) {
            this.name = name;
            register(this);
        }

        public static void register(EffectInfluenced influenced) {
            byName.put(influenced.name.toLowerCase(), influenced);
        }

        public VillageRank getRank() {
            return VillageRank.get(name);
        }

        public static EffectInfluenced valueOf(String string) {
            EffectInfluenced influenced = byName.get(string.toLowerCase());
            if (influenced == null) {
                throw new IllegalArgumentException("No value for " + string + " exists!");
            }
            return influenced;
        }

        @Override
        public String name() {
            return name;
        }
    }
}
