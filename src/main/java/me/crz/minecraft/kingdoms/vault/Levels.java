package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.village.VillageState;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Levels {
    public static final int VILLAGE_TO_KINGDOM_LEVEL = 5;
    public static final int KINGDOMS_MAX_LEVEL = 10;
    private static final Map<String, Map<String, Long>> LEVEL_REQUIREMENTS = new HashMap<>();

    public Levels() {
        super();
    }
    
    public static Map<String, Long> getLevelRequirements(VillageState state, int level) {
        return LEVEL_REQUIREMENTS.computeIfAbsent(state.name() + "_" + level, key -> getLevelRequirements(state.name().toLowerCase() + ".level-" + level));
    }

    private static Map<String, Long> getLevelRequirements(String levelPath) {
        Map<String, Long> levelRequirements = new HashMap<>();
        FileConfiguration config = ConfigManager.getInstance().getLevelConfig();
        ConfigurationSection levelSection = config.getConfigurationSection(levelPath + ".requirements");
        if (levelSection != null) {
            for (String key : levelSection.getKeys(false)) {
                long amount = levelSection.getLong(key);
                if (key.equalsIgnoreCase("CASH")) {
                    levelRequirements.put("CASH", amount);
                } else {
                    Material material = Material.getMaterial(key.toUpperCase());
                    if (material != null) {
                        levelRequirements.put(material.name(), amount);
                    } else {
                        KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, key + " is not a valid requirement (either cash or a material name) for " + levelPath);
                    }
                }
            }
        } else {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, levelPath + " has no requirements but it seems to be below the max level?");
        }
        return levelRequirements;
    }

    public static void clearRequirements() {
        LEVEL_REQUIREMENTS.clear();
    }
}
