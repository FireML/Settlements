package de.themoep.settlements.npc;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.perk.NpcPerk;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class NpcManager {

    private static NpcManager instance;

    private List<NpcLevel> settlementLevels = new ArrayList<>();
    private Map<String, NpcPerk> settlementPerks = new LinkedHashMap<>();

    private List<NpcLevel> empireLevels = new ArrayList<>();
    private Map<String, NpcPerk> empirePerks = new LinkedHashMap<>();

    private NpcManager() {
        loadConfig();
    }

    public void loadConfig() {
        loadLevels("settlement", settlementLevels, settlementPerks);
        loadLevels("empire", empireLevels, empirePerks);
    }

    private void loadLevels(String type, List<NpcLevel> levels, Map<String, NpcPerk> perks) {
        levels.clear();

        for (NpcType npcType : NpcType.values()) {
            ConfigurationSection config = ConfigManager.getInstance().getNpcLevelConfig().getConfigurationSection(type + "." + npcType.getKey());
            int amount = config.getKeys(false).size();
            for (int i = 1; i <= amount; i++) {
                NpcLevel level = new NpcLevel(i, config.getConfigurationSection("level-" + i));
                levels.add(level);
                SettlementsPlugin.logDebug("Loaded " + type + " " + npcType + " " + level);

                ConfigurationSection perksConfig = config.getConfigurationSection("level-" + i + ".perks");
                if (perksConfig != null) {
                    for (String perkName : perksConfig.getKeys(false)) {
                        try {
                            ConfigurationSection perkConfig = perksConfig.getConfigurationSection(perkName);
                            if (perkConfig != null) {
                                perkConfig.set("level", i);
                                NpcPerk perk = new NpcPerk(perkConfig);
                                if (perks.putIfAbsent(perk.name().toLowerCase(Locale.ROOT), perk) != null) {
                                    SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Duplicate " + type + " NPC perk " + perkName + "!");
                                }
                                switch (type) {
                                    case "settlement":
                                        SettlementManager.getInstance().getUnlockables().addAll(perk.getUnlockables());
                                        break;
                                    case "empire":
                                        EmpireManager.getInstance().getUnlockables().addAll(perk.getUnlockables());
                                        break;
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Config for " + type + " " + npcType + " NPC Perk " + perkName + " was invalid! " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public static NpcManager getInstance() {
        if (instance == null) {
            instance = new NpcManager();
        }
        return instance;
    }

    private NpcLevel getLevelInternal(LandOwner<?, ?> owner, int level) {
        if (owner instanceof Settlement) {
            if (settlementLevels.size() <= level) {
                return settlementLevels.get(level - 1);
            }
        } else if (owner instanceof Empire) {
            if (empireLevels.size() <= level) {
                return empireLevels.get(level - 1);
            }
        }
        return null;
    }

    public Optional<NpcLevel> getLevel(LandOwner<?, ?> owner, int level) {
        return Optional.ofNullable(getLevelInternal(owner, level));
    }

    public NpcPerk getPerk(LandOwner<?, ?> owner, String name) {
        if (owner instanceof Settlement) {
            return settlementPerks.get(name.toLowerCase(Locale.ROOT));
        } else if (owner instanceof Empire) {
            return empirePerks.get(name.toLowerCase(Locale.ROOT));
        }
        return null;
    }


}
