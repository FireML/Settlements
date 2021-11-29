package de.themoep.settlements.rank;

import de.themoep.settlements.utils.config.ConfigManager;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RankManager {
    private static RankManager instance;

    private Map<String, EmpireRank> empireRanks = new LinkedHashMap<>();
    private EmpireRank empireLeaderRank = null;
    private EmpireRank empireDefaultRank = null;
    private Map<String, PermissionGroup> empirePermGroups = new LinkedHashMap<>();

    private Map<String, SettlementRank> settlementRanks = new LinkedHashMap<>();
    private Map<String, SettlementRank> settlementRanksByTitle = new HashMap<>();

    private SettlementRank settlementChiefRank = null;
    private SettlementRank settlementDefaultRank = null;
    private Map<String, PermissionGroup> settlementPermGroups = new LinkedHashMap<>();

    private RankManager() {
        loadEmpireRanks();
        loadSettlementRanks();
        SettlementsPlugin.logDebug("Loaded RankManager with " + settlementRanks.size() + " settlement ranks and " + empireRanks.size() + " empire ranks");
    }

    private void loadSettlementRanks() {
        Configuration config = ConfigManager.getInstance().getSettlementRanksConf().getConfig();
        ConfigurationSection ranks = config.getConfigurationSection("ranks");
        for (String rankName : ranks.getKeys(false)) {
            settlementRanks.put(rankName.toLowerCase(), new SettlementRank(rankName, ranks.getConfigurationSection(rankName)));
        }
        SettlementRank lastRank = null;
        for (SettlementRank rank : settlementRanks.values()) {
            if (settlementChiefRank == null) {
                settlementChiefRank = rank;
            }
            if (lastRank != null) {
                rank.setParent(lastRank.name());
                rank.setOrdinal(lastRank.ordinal() + 1);
            } else {
                rank.setOrdinal(0);
            }
            lastRank = rank;
        }
        settlementDefaultRank = lastRank;
        for (SettlementRank rank : new ArrayList<>(settlementRanks.values())) {
            for (SettlementState state : SettlementState.values()) {
                for (String title : rank.getTitles(state)) {
                    settlementRanksByTitle.put(title.toLowerCase(), rank);
                }
            }
        }

        ConfigurationSection groups = config.getConfigurationSection("perm-groups");
        for (String groupName : groups.getKeys(false)) {
            settlementPermGroups.put(groupName.toLowerCase(), new PermissionGroup(groups.getConfigurationSection(groupName)));
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

        ConfigurationSection groups = config.getConfigurationSection("perm-groups");
        for (String groupName : groups.getKeys(false)) {
            empirePermGroups.put(groupName.toLowerCase(), new PermissionGroup(groups.getConfigurationSection(groupName)));
        }
    }

    synchronized public static RankManager getInstance() {
        if (instance == null) {
            instance = new RankManager();
        }
        return instance;
    }

    public Collection<SettlementRank> getSettlementRanks() {
        return settlementRanks.values();
    }

    public Collection<EmpireRank> getEmpireRanks() {
        return empireRanks.values();
    }

    public SettlementRank getSettlementRank(String name) {
        return name != null ? settlementRanks.getOrDefault(name.toLowerCase(), settlementRanksByTitle.get(name.toLowerCase())) : null;
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

    public SettlementRank getSettlementChiefRank() {
        return settlementChiefRank;
    }

    public SettlementRank getSettlementDefaultRank() {
        return settlementDefaultRank;
    }

    boolean isToggleableEmpirePerm(String permission) {
        for (PermissionGroup group : empirePermGroups.values()) {
            if (group.getPermissions().contains(permission.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public Collection<PermissionGroup> getEmpirePermGroups() {
        return empirePermGroups.values();
    }

    PermissionGroup getEmpirePermGroup(String group) {
        return empirePermGroups.get(group.toLowerCase());
    }

    boolean isToggleableSettlementPerm(String permission) {
        for (PermissionGroup group : settlementPermGroups.values()) {
            if (group.getPermissions().contains(permission.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public Collection<PermissionGroup> getSettlementPermGroups() {
        return settlementPermGroups.values();
    }

    PermissionGroup getSettlementPermGroup(String group) {
        return settlementPermGroups.get(group.toLowerCase());
    }

}
