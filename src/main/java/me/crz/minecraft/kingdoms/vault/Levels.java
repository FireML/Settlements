package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.village.VillageState;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class Levels {
    public static final int VILLAGE_TO_KINGDOM_LEVEL = 5;
    private static final Map<String, Map<String, Long>> LEVEL_REQUIREMENTS = new HashMap<>();

    public Levels() {
        super();
    }
    
    public static Map<String, Long> getLevelRequirements(VillageState state, int level) {
        return LEVEL_REQUIREMENTS.computeIfAbsent(state.name() + "_" + level, key -> getLevelRequirements(state.name().toLowerCase() + ".level-" + level + ".requirements"));
    }

    private static Map<String, Long> getLevelRequirements(String levelPath) {
        Map<String, Long> levelRequirements = new HashMap<>();
        FileConfiguration config = ConfigManager.getInstance().getLevelConfig();
        for (String key : config.getConfigurationSection(levelPath).getKeys(false)) {
            if (key.equalsIgnoreCase("CASH")) {
                levelRequirements.put("CASH", config.getLong(levelPath + "." + key));
            } else {
                Material material = Material.getMaterial(key);
                levelRequirements.put(material.name(), config.getLong(levelPath + "." + key));
            }
        }
        return levelRequirements;
    }
}
