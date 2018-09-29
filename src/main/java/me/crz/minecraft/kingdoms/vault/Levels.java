package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.village.VillageState;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class Levels {
    public static final int VILLAGE_TO_KINGDOM_LEVEL = 5;
    private static final Map<String, Map<String, Integer>> LEVEL_REQUIREMENTS = new HashMap<>();

    public Levels() {
        super();
    }
    
    private static Map<String, Integer> getLevelRequirements(VillageState state, int level) {
        return LEVEL_REQUIREMENTS.computeIfAbsent(state.name() + "_" + level, key -> getLevelRequirements(state.name().toLowerCase() + ".level-" + level + ".requirements"));
    }

    private static Map<String, Integer> getLevelRequirements(String levelPath) {
        Map<String, Integer> levelRequirements = new HashMap<>();
        FileConfiguration config = ConfigManager.getInstance().getLevelConfig();
        for (String key : config.getConfigurationSection(levelPath).getKeys(false)) {
            if (key.equalsIgnoreCase("CASH")) {
                int amount = config.getInt(String.valueOf(levelPath) + "." + key);
                levelRequirements.put("CASH", amount);
            } else {
                Material material = Material.getMaterial(key);
                int amount2 = config.getInt(String.valueOf(levelPath) + "." + key);
                levelRequirements.put(material.name(), amount2);
            }
        }
        return levelRequirements;
    }

    public static Map<String, Integer> getLevelMapFromInt(int level, VillageState state) {
        return getLevelRequirements(state, level);
    }
}
