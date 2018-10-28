package me.crz.minecraft.kingdoms.perk;

import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Perk implements NameTag {
    private String name;
    private Material icon = Material.LINGERING_POTION;

    private int repeat = 0;
    private long cooldown = 0;
    private int level;
    private List<PerkEffect> effects = new ArrayList<>();
    private int bonusClaims = 0;
    private int bonusOutposts = 0;
    private List<String> commands = new ArrayList<>();
    private Map<String, Long> unlockCost = new HashMap<>();
    private Map<String, Long> repeatCost = new HashMap<>();

    public Perk(ConfigurationSection config) throws IllegalArgumentException {
        this.name = config.getName();
        this.icon = Material.valueOf(config.getString("icon", icon.name()).toUpperCase());
        this.repeat = config.getInt("repeat", repeat);
        this.cooldown = config.getLong("cooldown", cooldown);
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

}
