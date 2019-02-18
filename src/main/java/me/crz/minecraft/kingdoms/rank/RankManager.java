package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RankManager {
    private static RankManager instance;

    private Map<String, EmpireRank> empireRanks = new LinkedHashMap<>();
    private EmpireRank empireLeaderRank = null;
    private EmpireRank empireDefaultRank = null;
    private Set<String> toggleableEmpirePerms = new LinkedHashSet<>();

    private Map<String, VillageRank> villageRanks = new LinkedHashMap<>();
    private Map<String, VillageRank> villageRanksbyTitle = new HashMap<>();

    private VillageRank villageChiefRank = null;
    private VillageRank villageDefaultRank = null;
    private Set<String> toggleableVillagePerms = new LinkedHashSet<>();

    private RankManager() {
        loadEmpireRanks();
        loadVillageRanks();
    }

    private void loadVillageRanks() {
        Configuration config = ConfigManager.getInstance().getSettlementRanksConf().getConfig();
        ConfigurationSection ranks = config.getConfigurationSection("ranks");
        for (String rankName : ranks.getKeys(false)) {
            villageRanks.put(rankName.toLowerCase(), new VillageRank(rankName, ranks.getConfigurationSection(rankName)));
        }
        VillageRank lastRank = null;
        for (VillageRank rank : villageRanks.values()) {
            if (villageChiefRank == null) {
                villageChiefRank = rank;
            }
            if (lastRank != null) {
                rank.setParent(lastRank.name());
                rank.setOrdinal(lastRank.ordinal() + 1);
            } else {
                rank.setOrdinal(0);
            }
            lastRank = rank;
        }
        villageDefaultRank = lastRank;
        for (VillageRank rank : new ArrayList<>(villageRanks.values())) {
            for (VillageState state : VillageState.values()) {
                for (String title : rank.getTitles(state)) {
                    villageRanksbyTitle.put(title.toLowerCase(), rank);
                }
            }
        }

        for (String perm : config.getStringList("toggleable-perms")) {
            toggleableVillagePerms.add(perm.toLowerCase());
        }
    }

    private void loadEmpireRanks() {
        Configuration config = ConfigManager.getInstance().getEmpireRanksConf().getConfig();
        ConfigurationSection ranks = config.getConfigurationSection("ranks");
        for (String rankName : ranks.getKeys(false)) {
            empireRanks.put(rankName.toLowerCase(), new EmpireRank(rankName, ranks.getConfigurationSection(rankName)));
        }
        EmpireRank lastRank = null;
        for (EmpireRank rank : empireRanks.values()) {
            if (empireLeaderRank == null) {
                empireLeaderRank = rank;
            }
            if (lastRank != null) {
                rank.setParent(lastRank.name());
                rank.setOrdinal(lastRank.ordinal() + 1);
            } else {
                rank.setOrdinal(0);
            }
            lastRank = rank;
        }
        empireDefaultRank = lastRank;

        for (String perm : config.getStringList("toggleable-perms")) {
            toggleableEmpirePerms.add(perm.toLowerCase());
        }
    }

    public static RankManager getInstance() {
        if (instance == null) {
            instance = new RankManager();
        }
        return instance;
    }

    public Collection<VillageRank> getVillageRanks() {
        return villageRanks.values();
    }

    public Collection<EmpireRank> getEmpireRanks() {
        return empireRanks.values();
    }

    public VillageRank getVillageRank(String name) {
        return name != null ? villageRanks.getOrDefault(name.toLowerCase(), villageRanksbyTitle.get(name.toLowerCase())) : null;
    }

    public EmpireRank getEmpireRank(String name) {
        return name != null ? empireRanks.get(name.toLowerCase()) : null;
    }

    public EmpireRank getEmpireLeaderRank() {
        return empireLeaderRank;
    }

    public EmpireRank getEmpireDefaultRank() {
        return empireDefaultRank;
    }

    public VillageRank getVillageChiefRank() {
        return villageChiefRank;
    }

    public VillageRank getVillageDefaultRank() {
        return villageDefaultRank;
    }

    Set<String> getToggleableEmpirePerms() {
        return toggleableEmpirePerms;
    }

    Set<String> getToggleableVillagePerms() {
        return toggleableVillagePerms;
    }
}
