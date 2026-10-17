package de.themoep.settlements.rank;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.utils.config.ConfigManager;
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

    private Map<String, SettlementRank> settlementRanks;
    private Map<String, SettlementRank> settlementRanksByTitle;

    private SettlementRank settlementChiefRank = null;
    private SettlementRank settlementDefaultRank = null;
    private static final String settlementGroupPrefix = "settlement.group.";
    private Map<String, PermissionGroup> settlementPermGroups;
    private Collection<String> settlementToggleablePerms;

    private void setup() {
        loadSettlementRanks();
        SettlementsPlugin.logDebug("Loaded RankManager with " + settlementRanks.size() + " settlement ranks");
    }

    private void loadSettlementRanks() {
        Configuration config = ConfigManager.getInstance().getSettlementRanksConf().getConfig();
        settlementPermGroups = new LinkedHashMap<>();
        ConfigurationSection groups = config.getConfigurationSection("perm-groups");
        for (String groupName : groups.getKeys(false)) {
            try {
                settlementPermGroups.put(groupName.toLowerCase(), new PermissionGroup(groups.getConfigurationSection(groupName)));
            } catch (InvalidConfigurationException e) {
                SettlementsPlugin.getInstance().getLogging().error("Error while loading " + groupName + " settlement group! " + e.getMessage());
            }
        }

        settlementRanks = new LinkedHashMap<>();
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

        settlementToggleablePerms = new LinkedHashSet<>();
        for (String permission : config.getStringList("toggleable-permissions")) {
            if (settlementChiefRank.getPermissionValue(permission)) {
                settlementToggleablePerms.add(permission);
            } else {
                SettlementsPlugin.getInstance().getLogging().warn("Cannot make settlement permission '" + permission + "' toggleable as chief rank doesn't have it!");
            }
        }
        if (settlementToggleablePerms.isEmpty()) {
            settlementToggleablePerms.addAll(getSettlementChiefRank().getInheritedPermissions());
        }
    }

    public static RankManager getInstance() {
        return instance;
    }

    public static void load() {
        if (instance == null) {
            instance = new RankManager();
            instance.setup();
        }
    }

    public Collection<SettlementRank> getSettlementRanks() {
        return settlementRanks.values();
    }

    public SettlementRank getSettlementRank(String name) {
        return name != null ? settlementRanks.getOrDefault(name.toLowerCase(), settlementRanksByTitle.get(name.toLowerCase())) : null;
    }

    public SettlementRank getSettlementChiefRank() {
        return settlementChiefRank;
    }

    public SettlementRank getSettlementDefaultRank() {
        return settlementDefaultRank;
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
     * Get a group by its ID string suffixed by settlements.group.
     * @param id    The group ID string
     * @return The group or null if no group exists
     * @throws IllegalArgumentException Thrown if the id string has an invalid suffix
     */
    public PermissionGroup getPermissionGroup(String id) {
        if (id.startsWith(settlementGroupPrefix)) {
            return getSettlementPermGroup(id.substring(settlementGroupPrefix.length()));
        }
        throw new IllegalArgumentException(id + " is not a valid group specification");
    }

    public Collection<String> getSettlementToggleablePerms() {
        return settlementToggleablePerms;
    }

    /**
     * Apply a method to all permissions in a group
     * @param groupId   The group id suffixed by settlements.group.
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
     * @param groupId   The group id suffixed by settlements.group.
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
