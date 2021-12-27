package de.themoep.settlements.npc;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandownerLevel;
import de.themoep.settlements.perk.NpcPerk;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.config.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public class NpcManager {

    private static NpcManager instance;

    private Table<String, NpcType, NpcConfig> npcConfigs = HashBasedTable.create();

    private Multimap<NpcType, NpcLevel> settlementLevels = MultimapBuilder.enumKeys(NpcType.class).arrayListValues().build();
    private Map<String, NpcPerk> settlementPerks = new LinkedHashMap<>();

    private Multimap<NpcType, NpcLevel> empireLevels = MultimapBuilder.enumKeys(NpcType.class).arrayListValues().build();
    private Map<String, NpcPerk> empirePerks = new LinkedHashMap<>();

    private void setup() {
        loadConfig();
    }

    public void loadConfig() {
        npcConfigs.clear();
        loadNpcs(NpcType.AppliesTo.SETTLEMENT);
        loadNpcs(NpcType.AppliesTo.EMPIRE);
        loadLevels(NpcType.AppliesTo.SETTLEMENT, settlementLevels, settlementPerks);
        loadLevels(NpcType.AppliesTo.EMPIRE, empireLevels, empirePerks);
    }

    private void loadNpcs(NpcType.AppliesTo type) {
        for (NpcType npcType : NpcType.values()) {
            if (!npcType.appliesTo(type)) {
                continue;
            }
            ConfigurationSection config = ConfigManager.getInstance().getNpcConfig().getConfigurationSection(type.getKey() + "." + npcType.getKey());
            if (config != null) {
                try {
                    npcConfigs.put(type.getKey(), npcType, new NpcConfig(config));
                } catch (IllegalArgumentException e) {
                    SettlementsPlugin.getInstance().getLogger().info("Error while loading " + type.getKey() + " NPC " + npcType.getKey() + ": " + e.getMessage());
                    npcConfigs.put(type.getKey(), npcType, new NpcConfig(npcType));
                }
            }
        }
    }

    private void loadLevels(NpcType.AppliesTo type, Multimap<NpcType, NpcLevel> levels, Map<String, NpcPerk> perks) {
        levels.clear();
        perks.clear();

        for (NpcType npcType : NpcType.values()) {
            if (!npcType.appliesTo(type)) {
                continue;
            }
            ConfigurationSection config = ConfigManager.getInstance().getNpcLevelConfig().getConfigurationSection(type.getKey() + "." + npcType.getKey());
            if (config != null) {
                int amount = config.getKeys(false).size();
                NpcLevel lastLevel = null;
                for (int i = 1; i <= amount; i++) {
                    ConfigurationSection levelConfig = config.getConfigurationSection("level-" + i);
                    NpcLevel level = new NpcLevel(i, lastLevel, levelConfig);
                    lastLevel = level;
                    levels.put(npcType, level);
                    SettlementsPlugin.logDebug("Loaded " + type + " " + npcType + " " + level);

                    for (String perkName : level.getPerks()) {
                        try {
                            ConfigurationSection perkConfig = levelConfig.getConfigurationSection("perks." + perkName);
                            if (perkConfig != null) {
                                perkConfig.set("level", i);
                                NpcPerk perk = new NpcPerk(npcType, perkConfig);
                                if (perks.putIfAbsent(perk.name().toLowerCase(Locale.ROOT), perk) != null) {
                                    SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Duplicate " + type + " NPC perk " + perkName + "!");
                                }
                                switch (type) {
                                    case SETTLEMENT:
                                        SettlementManager.getInstance().getUnlockables().addAll(perk.getUnlockables());
                                        break;
                                    case EMPIRE:
                                        EmpireManager.getInstance().getUnlockables().addAll(perk.getUnlockables());
                                        break;
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            SettlementsPlugin.getInstance().getLogger().log(Level.SEVERE, "Config for " + type + " " + npcType + " NPC Perk " + perkName + " was invalid! " + e.getMessage());
                        }
                    }
                }
            } else {
                SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, "No levels configured for " + type + " NPC " + npcType);
            }
        }
    }

    public static NpcManager getInstance() {
        return instance;
    }

    public static void load() {
        if (instance == null) {
            instance = new NpcManager();
            instance.setup();
        }
    }

    private NpcLevel getLevelInternal(LandOwner<?, ?> owner, NpcType type, int level) {
        List<NpcLevel> levels;
        if (owner instanceof Settlement) {
            levels = ((List<NpcLevel>) settlementLevels.get(type));
        } else if (owner instanceof Empire) {
            levels = ((List<NpcLevel>) empireLevels.get(type));
        } else {
            return null;
        }
        if (levels.size() >= level) {
            return levels.get(level - 1);
        }
        return null;
    }

    public Optional<NpcLevel> getLevel(LandOwner<?, ?> owner, NpcType type, int level) {
        return Optional.ofNullable(getLevelInternal(owner, type, level));
    }

    public Collection<NpcLevel> getLevels(LandOwner<?,?> owner, NpcType type) {
        if (owner instanceof Settlement) {
            return settlementLevels.get(type);
        } else if (owner instanceof Empire) {
            return empireLevels.get(type);
        }
        return Collections.emptyList();
    }

    public NpcPerk getPerk(LandOwner<?, ?> owner, String name) {
        if (owner instanceof Settlement) {
            return settlementPerks.get(name.toLowerCase(Locale.ROOT));
        } else if (owner instanceof Empire) {
            return empirePerks.get(name.toLowerCase(Locale.ROOT));
        }
        return null;
    }

    public NpcConfig getConfig(LandOwner<?, ?> landOwner, NpcType type) {
        return getConfig(landOwner.getKey(), type);
    }

    public NpcConfig getConfig(String ownerType, NpcType type) {
        return npcConfigs.get(ownerType, type);
    }

    public Set<Table.Cell<String, NpcType, NpcConfig>> getConfigs() {
        return npcConfigs.cellSet();
    }

    public Collection<NpcPerk> getPerks(LandOwner<?, ?> owner) {
        if (owner instanceof Settlement) {
            return settlementPerks.values();
        } else if (owner instanceof Empire) {
            return empirePerks.values();
        }
        return Collections.emptySet();
    }
}
