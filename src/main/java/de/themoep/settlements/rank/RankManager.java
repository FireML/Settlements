package de.themoep.settlements.rank;

import de.themoep.settlements.utils.config.ConfigManager;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class RankManager {
    private static RankManager instance;

    private Map<String, EmpireRank> empireRanks;
    private EmpireRank empireLeaderRank = null;
    private EmpireRank empireDefaultRank = null;
    private static final String empireGroupPrefix = "empire.group.";
    private Map<String, PermissionGroup> empirePermGroups;
    private Collection<String> empireToggleablePerms;

    private Map<String, SettlementRank> settlementRanks;
    private Map<String, SettlementRank> settlementRanksByTitle;

    private SettlementRank settlementChiefRank = null;
    private SettlementRank settlementDefaultRank = null;
    private static final String settlementGroupPrefix = "settlement.group.";
    private Map<String, PermissionGroup> settlementPermGroups;
    private Collection<String> settlementToggleablePerms;

    public void load() {
        loadEmpireRanks();
        loadSettlementRanks();
        SettlementsPlugin.logDebug("Loaded RankManager with " + settlementRanks.size() + " settlement ranks and " + empireRanks.size() + " empire ranks");
    }

    private void loadSettlementRanks() {
        settlementRanks = new LinkedHashMap<>();
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
        settlementRanksByTitle = new HashMap<>();
        for (SettlementRank rank : new ArrayList<>(settlementRanks.values())) {
            for (SettlementState state : SettlementState.values()) {
                for (String title : rank.getTitles(state)) {
                    settlementRanksByTitle.put(title.toLowerCase(), rank);
                }
            }
        }

        settlementPermGroups = new LinkedHashMap<>();
        ConfigurationSection groups = config.getConfigurationSection("perm-groups");
        for (String groupName : groups.getKeys(false)) {
            try {
                settlementPermGroups.put(groupName.toLowerCase(), new PermissionGroup(groups.getConfigurationSection(groupName)));
            } catch (InvalidConfigurationException e) {
                SettlementsPlugin.getInstance().getLogger().severe("Error while loading " + groupName + " settlement group! " + e.getMessage());
            }
        }

        settlementToggleablePerms = new LinkedHashSet<>();
        for (String permission : config.getStringList("toggleable-perms")) {
            if (settlementChiefRank.getPermissionValue(permission)) {
                settlementToggleablePerms.add(permission);
            } else {
                SettlementsPlugin.getInstance().getLogger().warning("Cannot make settlement permission '" + permission + "' toggleable as chief rank doesn't have it!");
            }
        }
        if (settlementToggleablePerms.isEmpty()) {
            settlementToggleablePerms.addAll(getSettlementChiefRank().getInheritedPermissions());
        }
    }

    private void loadEmpireRanks() {
        empireRanks = new LinkedHashMap<>();
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

        empirePermGroups = new LinkedHashMap<>();
        ConfigurationSection groups = config.getConfigurationSection("perm-groups");
        for (String groupName : groups.getKeys(false)) {
            try {
                empirePermGroups.put(groupName.toLowerCase(), new PermissionGroup(groups.getConfigurationSection(groupName)));
            } catch (InvalidConfigurationException e) {
                SettlementsPlugin.getInstance().getLogger().severe("Error while loading " + groupName + " empire group! " + e.getMessage());
            }
        }

        empireToggleablePerms = new LinkedHashSet<>();
        for (String permission : config.getStringList("toggleable-perms")) {
            if (empireLeaderRank.getPermissionValue(permission)) {
                empireToggleablePerms.add(permission);
            } else {
                SettlementsPlugin.getInstance().getLogger().warning("Cannot make empire permission '" + permission + "' toggleable as leader rank doesn't have it!");
            }
        }
        if (empireToggleablePerms.isEmpty()) {
            empireToggleablePerms.addAll(getEmpireLeaderRank().getInheritedPermissions());
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
        return getEmpireToggleablePerms().contains(permission.toLowerCase(Locale.ROOT));
    }

    public Collection<PermissionGroup> getEmpirePermGroups() {
        return empirePermGroups.values();
    }

    PermissionGroup getEmpirePermGroup(String group) {
        return empirePermGroups.get(group.toLowerCase());
    }

    boolean isToggleableSettlementPerm(String permission) {
        return getSettlementToggleablePerms().contains(permission.toLowerCase(Locale.ROOT));
    }

    public Collection<PermissionGroup> getSettlementPermGroups() {
        return settlementPermGroups.values();
    }

    PermissionGroup getSettlementPermGroup(String group) {
        return settlementPermGroups.get(group.toLowerCase());
    }

    /**
     * Get a group by its ID string suffixed by either settlements.group. or empire.group.
     * @param id    The group ID string
     * @return The group or null if no group exists
     * @throws IllegalArgumentException Thrown if the id string has an invalid suffix
     */
    public PermissionGroup getPermissionGroup(String id) {
        if (id.startsWith(settlementGroupPrefix)) {
            return getSettlementPermGroup(id.substring(settlementGroupPrefix.length()));
        }
        if (id.startsWith(empireGroupPrefix)) {
            return getEmpirePermGroup(id.substring(empireGroupPrefix.length()));
        }
        throw new IllegalArgumentException(id + " is not a valid group specification");
    }

    public Collection<String> getEmpireToggleablePerms() {
        return empireToggleablePerms;
    }

    public Collection<String> getSettlementToggleablePerms() {
        return settlementToggleablePerms;
    }

    /**
     * Apply a method to all permissions in a group
     * @param groupId   The group id suffixed by either settlements.group. or empire.group.
     * @param handler   What Boolean function to apply
     * @return An optional with either the result of the handler or empty if the group couldn't be found
     * @throws IllegalStateException Thrown if the group doesn't have any permissions
     * @throws IllegalArgumentException Thrown if the id string has an invalid suffix
     */
    public Optional<Boolean> applyToGroup(String groupId, Function<String, Boolean> handler) {
        if (groupId.contains(".group.")) {
            PermissionGroup group = RankManager.getInstance().getPermissionGroup(groupId);
            if (group != null) {
                if (group.getPermissions().isEmpty()) throw new IllegalStateException(groupId + " has no permissions define!");
                boolean r = true;
                for (String perm : group.getPermissions()) {
                    r &= handler.apply(perm);
                }
                return Optional.of(r);
            }
            return Optional.empty();
        }
        throw new IllegalArgumentException(groupId + " is not a valid group id!");
    }

    /**
     * Apply a function to the first permission of a group
     * @param groupId   The group id suffixed by either settlements.group. or empire.group.
     * @param handler   What Boolean function to apply
     * @return An optional with either the result of the handler or empty if the group couldn't be found
     * @throws IllegalStateException Thrown if the group doesn't have any permissions
     * @throws IllegalArgumentException Thrown if the id string has an invalid suffix
     */
    public Optional<Boolean> getFromGroup(String groupId, Function<String, Optional<Boolean>> handler) {
        if (groupId.contains(".group.")) {
            PermissionGroup group = RankManager.getInstance().getPermissionGroup(groupId);
            if (group != null) {
                if (group.getPermissions().isEmpty()) throw new IllegalStateException(groupId + " has no permissions define!");
                for (String perm : group.getPermissions()) {
                    return handler.apply(perm);
                }
            }
            return Optional.empty();
        }
        throw new IllegalArgumentException(groupId + " is not a valid group id!");
    }
}
