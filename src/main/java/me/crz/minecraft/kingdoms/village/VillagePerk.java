package me.crz.minecraft.kingdoms.village;

import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VillagePerk implements NameTag {
    private String name;
    private Material icon = Material.LINGERING_POTION;

    private int repeat = 0;
    private long cooldown = 0;
    private VillageState state = VillageState.VILLAGE;
    private int level;
    private List<PerkEffect> effects = new ArrayList<>();
    private int bonusClaims = 0;
    private int bonusOutposts = 0;
    private List<String> commands = new ArrayList<>();
    private Map<String, Long> unlockCost = new HashMap<>();
    private Map<String, Long> repeatCost = new HashMap<>();

    public VillagePerk(ConfigurationSection config) throws IllegalArgumentException {
        this.name = config.getName();
        this.icon = Material.valueOf(config.getString("icon", icon.name()).toUpperCase());
        this.repeat = config.getInt("repeat", repeat);
        this.cooldown = config.getLong("cooldown", cooldown);
        this.state = VillageState.valueOf(config.getString("state", state.name()).toUpperCase());
        if (config.isInt("level")) {
            this.level = config.getInt("level");
        } else {
            throw new IllegalArgumentException("Config does not contain level!");
        }
        for (Map<?, ?> map : config.getMapList("effects")) {
            try {
                effects.add(new PerkEffect((Map<String, Object>) map));
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Effect is not valid! " + e.getMessage());
            }
        }
        this.bonusClaims = config.getInt("bonus.claims", bonusClaims);
        this.bonusOutposts = config.getInt("bonus.outposts", bonusOutposts);
        this.commands = config.getStringList("run-command");
        if (config.isConfigurationSection("cost.unlock")) {
            for (String type : config.getConfigurationSection("cost.unlock").getKeys(false)) {
                unlockCost.put(type, config.getLong("cost.unlock." + type));
            }
        }
        if (config.isConfigurationSection("cost.repeating")) {
            for (String type : config.getConfigurationSection("cost.repeating").getKeys(false)) {
                repeatCost.put(type, config.getLong("cost.repeating." + type));
            }
        }
    }

    @Override
    public String name() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public int getRepeat() {
        return repeat;
    }

    public long getCooldown() {
        return cooldown;
    }

    public VillageState getState() {
        return state;
    }

    public int getLevel() {
        return level;
    }

    public List<PerkEffect> getEffects() {
        return effects;
    }

    public int getBonusClaims() {
        return bonusClaims;
    }

    public int getBonusOutposts() {
        return bonusOutposts;
    }

    public List<String> getCommands() {
        return commands;
    }

    public Map<String, Long> getUnlockCost() {
        return unlockCost;
    }

    public Map<String, Long> getRepeatCost() {
        return repeatCost;
    }

    public boolean hasEffects() {
        return !effects.isEmpty();
    }

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
            return new PotionEffect(type, length > 0 ? length * 20: 300, strength, ambient, particles, icon);
        }
    }

    public enum EffectLocation implements NameTag {
        PLOT,
        PLOT_FRIENDS,
        OWN_SETTLEMENT,
        ALLIED_SETTLEMENT,
        ENEMY_SETTLEMENT,
        UNCLAIMED,
        EVERYWHERE
    }

    public enum EffectInfluenced implements NameTag {
        RESIDENT(VillageRank.RESIDENT),
        RECRUITER(VillageRank.RECRUITER),
        BUILDER(VillageRank.BUILDER),
        ELDER(VillageRank.ELDER),
        CHIEF(VillageRank.CHIEF),
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
