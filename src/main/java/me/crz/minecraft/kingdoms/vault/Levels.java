package me.crz.minecraft.kingdoms.vault;

import me.crz.minecraft.kingdoms.village.VillageState;
import java.util.Iterator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import java.util.HashMap;

public class Levels
{
    public static final int VILLAGE_TO_KINGDOM_LEVEL = 5;
    private static final HashMap<String, Integer> VILLAGE_LEVEL_TWO;
    private static final HashMap<String, Integer> VILLAGE_LEVEL_THREE;
    private static final HashMap<String, Integer> VILLAGE_LEVEL_FOUR;
    private static final HashMap<String, Integer> VILLAGE_LEVEL_FIVE;
    private static final HashMap<String, Integer> KINGDOM_LEVEL_ONE;
    private static final HashMap<String, Integer> KINGDOM_LEVEL_TWO;
    private static final HashMap<String, Integer> KINGDOM_LEVEL_THREE;
    private static final HashMap<String, Integer> KINGDOM_LEVEL_FOUR;
    private static final HashMap<String, Integer> KINGDOM_LEVEL_FIVE;
    private static final HashMap<String, Integer> KINGDOM_LEVEL_SIX;
    private static final HashMap<String, Integer> KINGDOM_LEVEL_SEVEN;
    private static final HashMap<String, Integer> KINGDOM_LEVEL_EIGHT;
    private static final HashMap<String, Integer> KINGDOM_LEVEL_NINE;
    private static final HashMap<String, Integer> KINGDOM_LEVEL_TEN;
    
    static {
        VILLAGE_LEVEL_TWO = getLevelRequirements("village-level-two");
        VILLAGE_LEVEL_THREE = getLevelRequirements("village-level-three");
        VILLAGE_LEVEL_FOUR = getLevelRequirements("village-level-four");
        VILLAGE_LEVEL_FIVE = getLevelRequirements("village-level-five");
        KINGDOM_LEVEL_ONE = getLevelRequirements("kingdom-level-one");
        KINGDOM_LEVEL_TWO = getLevelRequirements("kingdom-level-two");
        KINGDOM_LEVEL_THREE = getLevelRequirements("kingdom-level-three");
        KINGDOM_LEVEL_FOUR = getLevelRequirements("kingdom-level-four");
        KINGDOM_LEVEL_FIVE = getLevelRequirements("kingdom-level-five");
        KINGDOM_LEVEL_SIX = getLevelRequirements("kingdom-level-six");
        KINGDOM_LEVEL_SEVEN = getLevelRequirements("kingdom-level-seven");
        KINGDOM_LEVEL_EIGHT = getLevelRequirements("kingdom-level-eight");
        KINGDOM_LEVEL_NINE = getLevelRequirements("kingdom-level-nine");
        KINGDOM_LEVEL_TEN = getLevelRequirements("kingdom-level-ten");
    }
    
    public Levels() {
        super();
    }
    
    private static HashMap<String, Integer> getLevelRequirements(final String levelPath) {
        final HashMap<String, Integer> levelRequirements = new HashMap<String, Integer>();
        final FileConfiguration config = ConfigManager.getInstance().getLevelConfig();
        for (final String key : config.getConfigurationSection(levelPath).getKeys(false)) {
            if (key.equalsIgnoreCase("CASH")) {
                final int amount = config.getInt(String.valueOf(levelPath) + "." + key);
                levelRequirements.put("CASH", amount);
            }
            else {
                final Material material = Material.getMaterial(key);
                final int amount2 = config.getInt(String.valueOf(levelPath) + "." + key);
                levelRequirements.put(material.name(), amount2);
            }
        }
        return levelRequirements;
    }
    
    public static HashMap<String, Integer> getLevelMapFromInt(final int level, final VillageState state) {
        if (level == 2 && state == VillageState.VILLAGE) {
            return VILLAGE_LEVEL_TWO;
        }
        if (level == 3 && state == VillageState.VILLAGE) {
            return VILLAGE_LEVEL_THREE;
        }
        if (level == 4 && state == VillageState.VILLAGE) {
            return VILLAGE_LEVEL_FOUR;
        }
        if (level == 5 && state == VillageState.VILLAGE) {
            return VILLAGE_LEVEL_FIVE;
        }
        if (level == 6 && state == VillageState.VILLAGE) {
            return KINGDOM_LEVEL_ONE;
        }
        if (level == 2 && state == VillageState.KINGDOM) {
            return KINGDOM_LEVEL_TWO;
        }
        if (level == 3 && state == VillageState.KINGDOM) {
            return KINGDOM_LEVEL_THREE;
        }
        if (level == 4 && state == VillageState.KINGDOM) {
            return KINGDOM_LEVEL_FOUR;
        }
        if (level == 5 && state == VillageState.KINGDOM) {
            return KINGDOM_LEVEL_FIVE;
        }
        if (level == 6 && state == VillageState.KINGDOM) {
            return KINGDOM_LEVEL_SIX;
        }
        if (level == 7 && state == VillageState.KINGDOM) {
            return KINGDOM_LEVEL_SEVEN;
        }
        if (level == 8 && state == VillageState.KINGDOM) {
            return KINGDOM_LEVEL_EIGHT;
        }
        if (level == 9 && state == VillageState.KINGDOM) {
            return KINGDOM_LEVEL_NINE;
        }
        if (level == 10 && state == VillageState.KINGDOM) {
            return KINGDOM_LEVEL_TEN;
        }
        return null;
    }
}
