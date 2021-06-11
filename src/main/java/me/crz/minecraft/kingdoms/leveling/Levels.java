package me.crz.minecraft.kingdoms.leveling;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.empire.EmpireLevel;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.VillageLevel;
import me.crz.minecraft.kingdoms.village.VillageState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Levels {

    private static Levels instance;

    private Map<VillageState, List<VillageLevel>> settlementLevels = new HashMap<>();
    private List<EmpireLevel> empireLevels = new ArrayList<>();

    private Levels() {
        load();
    }

    public void load() {
        settlementLevels.clear();
        List<VillageLevel> villageLevels = new ArrayList<>();
        for (int i = 1; i <= Config.Settlement.Leveling.VILLAGE_MAX_LEVEL; i++) {
            VillageLevel level = new VillageLevel(i, ConfigManager.getInstance().getLevelConfig().getConfigurationSection("village.level-" + i));
            villageLevels.add(level);
            KingdomsPlugin.logDebug("Loaded " + level);
        }
        settlementLevels.put(VillageState.VILLAGE, villageLevels);

        List<VillageLevel> kingdomLevels = new ArrayList<>();
        for (int i = 1; i <= Config.Settlement.Leveling.KINGDOM_MAX_LEVEL; i++) {
            VillageLevel level = new VillageLevel(i, ConfigManager.getInstance().getLevelConfig().getConfigurationSection("kingdom.level-" + i));
            kingdomLevels.add(level);
            KingdomsPlugin.logDebug("Loaded " + level);
        }
        settlementLevels.put(VillageState.KINGDOM, kingdomLevels);

        empireLevels.clear();
        for (int i = 1; i <= Config.Empire.Leveling.MAX_LEVEL; i++) {
            EmpireLevel level = new EmpireLevel(i, ConfigManager.getInstance().getEmpireLevelConfig().getConfigurationSection("levels.level-" + i));
            empireLevels.add(level);
            KingdomsPlugin.logDebug("Loaded " + level);
        }
    }

    public static Levels getInstance() {
        if (instance == null) {
            instance = new Levels();
        }
        return instance;
    }

    public Optional<VillageLevel> getLevel(VillageState state, int level) {
        List<VillageLevel> levels = settlementLevels.get(state);
        if (levels != null && level <= levels.size()) {
            return Optional.of(levels.get(level - 1));
        }
        return Optional.empty();
    }

    public Optional<EmpireLevel> getEmpireLevel(int level) {
        if (level <= empireLevels.size()) {
            return Optional.of(empireLevels.get(level - 1));
        }
        return Optional.empty();
    }
}
