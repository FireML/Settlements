package de.themoep.settlements.rank;

import de.themoep.settlements.utils.config.ConfigManager;
import de.themoep.settlements.village.VillageState;
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

    private Map<String, VillageRank> villageRanks = new LinkedHashMap<>();
    private Map<String, VillageRank> villageRanksbyTitle = new HashMap<>();

    private VillageRank villageChiefRank = null;
    private VillageRank villageDefaultRank = null;
    private Map<String, PermissionGroup> villagePermGroups = new LinkedHashMap<>();

    private RankManager() {
        loadEmpireRanks();
        loadVillageRanks();
        SettlementsPlugin.logDebug("Loaded RankManager with " + villageRanks.size() + " village ranks and " + empireRanks.size() + " empire ranks");
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

        ConfigurationSection groups = config.getConfigurationSection("perm-groups");
        for (String groupName : groups.getKeys(false)) {
            villagePermGroups.put(groupName.toLowerCase(), new PermissionGroup(groups.getConfigurationSection(groupName)));
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

    boolean isToggleableVillagePerm(String permission) {
        for (PermissionGroup group : villagePermGroups.values()) {
            if (group.getPermissions().contains(permission.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public Collection<PermissionGroup> getVillagePermGroups() {
        return villagePermGroups.values();
    }

    PermissionGroup getVillagePermGroup(String group) {
        return villagePermGroups.get(group.toLowerCase());
    }

}
