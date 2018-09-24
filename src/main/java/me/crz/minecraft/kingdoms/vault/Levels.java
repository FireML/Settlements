package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.village.VillageState;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;

import java.util.HashMap;

public class Levels {
    public static final int VILLAGE_TO_KINGDOM_LEVEL = 5;
    private static final HashMap<String, Integer> VILLAGE_LEVEL_TWO = getLevelRequirements("village-level-two");
    private static final HashMap<String, Integer> VILLAGE_LEVEL_THREE = getLevelRequirements("village-level-three");
    private static final HashMap<String, Integer> VILLAGE_LEVEL_FOUR = getLevelRequirements("village-level-four");
    private static final HashMap<String, Integer> VILLAGE_LEVEL_FIVE = getLevelRequirements("village-level-five");
    private static final HashMap<String, Integer> KINGDOM_LEVEL_ONE = getLevelRequirements("kingdom-level-one");
    private static final HashMap<String, Integer> KINGDOM_LEVEL_TWO = getLevelRequirements("kingdom-level-two");
    private static final HashMap<String, Integer> KINGDOM_LEVEL_THREE = getLevelRequirements("kingdom-level-three");
    private static final HashMap<String, Integer> KINGDOM_LEVEL_FOUR = getLevelRequirements("kingdom-level-four");
    private static final HashMap<String, Integer> KINGDOM_LEVEL_FIVE = getLevelRequirements("kingdom-level-five");
    private static final HashMap<String, Integer> KINGDOM_LEVEL_SIX = getLevelRequirements("kingdom-level-six");
    private static final HashMap<String, Integer> KINGDOM_LEVEL_SEVEN = getLevelRequirements("kingdom-level-seven");
    private static final HashMap<String, Integer> KINGDOM_LEVEL_EIGHT = getLevelRequirements("kingdom-level-eight");
    private static final HashMap<String, Integer> KINGDOM_LEVEL_NINE = getLevelRequirements("kingdom-level-nine");
    private static final HashMap<String, Integer> KINGDOM_LEVEL_TEN = getLevelRequirements("kingdom-level-ten");

    public Levels() {
        super();
    }

    private static HashMap<String, Integer> getLevelRequirements(final String levelPath) {
        final HashMap<String, Integer> levelRequirements = new HashMap<>();
        final FileConfiguration config = ConfigManager.getInstance().getLevelConfig();
        for (final String key : config.getConfigurationSection(levelPath).getKeys(false)) {
            if (key.equalsIgnoreCase("CASH")) {
                final int amount = config.getInt(String.valueOf(levelPath) + "." + key);
                levelRequirements.put("CASH", amount);
            } else {
                final Material material = Material.getMaterial(key);
                final int amount2 = config.getInt(String.valueOf(levelPath) + "." + key);
                levelRequirements.put(material.name(), amount2);
            }
        }
        return levelRequirements;
    }

    public static HashMap<String, Integer> getLevelMapFromInt(final int level, final VillageState state) {
        if (state == VillageState.VILLAGE) {
            switch (level) {
                case 2:
                    return VILLAGE_LEVEL_TWO;
                case 3:
                    return VILLAGE_LEVEL_THREE;
                case 4:
                    return VILLAGE_LEVEL_FOUR;
                case 5:
                    return VILLAGE_LEVEL_FIVE;
                case 6:
                    return KINGDOM_LEVEL_ONE;
            }
        } else if (state == VillageState.KINGDOM) {
            switch (level) {
                case 2:
                    return KINGDOM_LEVEL_TWO;
                case 3:
                    return KINGDOM_LEVEL_THREE;
                case 4:
                    return KINGDOM_LEVEL_FOUR;
                case 5:
                    return KINGDOM_LEVEL_FIVE;
                case 6:
                    return KINGDOM_LEVEL_SIX;
                case 7:
                    return KINGDOM_LEVEL_SEVEN;
                case 8:
                    return KINGDOM_LEVEL_EIGHT;
                case 9:
                    return KINGDOM_LEVEL_NINE;
                case 10:
                    return KINGDOM_LEVEL_TEN;
            }
        }
        return null;
    }
}
