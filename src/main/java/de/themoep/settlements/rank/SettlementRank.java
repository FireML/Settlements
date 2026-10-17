package de.themoep.settlements.rank;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.perk.PerkEffect;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.utils.MaterialUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class SettlementRank implements de.themoep.settlements.utils.NameTag, Comparable<SettlementRank>, de.themoep.settlements.utils.Next<SettlementRank>, Rankable {

    private final Multimap<SettlementState, String> titles = MultimapBuilder.enumKeys(SettlementState.class).arrayListValues().build();
    private final String name;
    private final LinkedHashSet<String> permissions = new LinkedHashSet<>();
    private int ordinal = Integer.MAX_VALUE;
    private String inherits = null;
    private String parent = null;
    private Material icon = Material.WOODEN_SHOVEL;

    public SettlementRank(String name, ConfigurationSection config) {
        this.name = name;
        if (config != null) {
            for (String permission : config.getStringList("permissions")) {
                String permName = permission.toLowerCase(Locale.ROOT);
                addPermission(permName);
                if (permName.contains(".group.")) {
                    PermissionGroup group = RankManager.getInstance().getPermissionGroup(permName);
                    if (group != null) {
                        for (String groupPermission : group.getPermissions()) {
                            addPermission(groupPermission);
                        }
                    }
                }
            }
            if (config.isString("icon")) {
                Material icon = MaterialUtils.getMaterial(config.getString("icon"));
                if (icon != null && !icon.isItem()) {
                    SettlementsPlugin.getInstance().getLogging().warn(icon + " is not a valid icon type for " + getClass().getSimpleName() + " " + name + "! That type has no item for the GUI!");
                    icon = null;
                }
                if (icon == null) {
                    icon = Material.WOODEN_SHOVEL;
                }
                this.icon = icon;
            }
            if (config.isString("inherits")) {
                inherits = config.getString("inherits");
            }
        }
        for (SettlementState state : SettlementState.values()) {
            List<String> titles = Message.get(state.getKey() + ".rank." + getKey()).getTextList().stream().map(StringUtils::stripColors).toList();
            this.titles.putAll(state, titles);
        }
        PerkEffect.EffectInfluenced.register(new PerkEffect.EffectInfluenced(name));
    }

    public SettlementRank getInheritedRank() {
        return get(getInherits());
    }

    public Collection<SettlementRank> getInheritedRanks() {
        List<SettlementRank> ranks = new ArrayList<>();
        SettlementRank rank = this;
        while (rank.getInherits() != null) {
            ranks.add(rank.getInheritedRank());
            rank = rank.getInheritedRank();
        }
        return ranks;
    }

    public SettlementRank getParentRank() {
        return get(getParent());
    }

    public static SettlementRank get(String string) {
        return RankManager.getInstance().getSettlementRank(string);
    }

    public static SettlementRank getDefault() {
        return RankManager.getInstance().getSettlementDefaultRank();
    }

    public static SettlementRank getChief() {
        return RankManager.getInstance().getSettlementChiefRank();
    }

    public boolean isChief() {
        return this == getChief();
    }

    public static boolean isToggleablePermission(String permission) {
        return RankManager.getInstance().isToggleableSettlementPerm(permission);
    }

    public static Collection<PermissionGroup> getPermGroups() {
        return RankManager.getInstance().getSettlementPermGroups();
    }

    public static PermissionGroup getPermGroup(String group) {
        return RankManager.getInstance().getSettlementPermGroup(group);
    }

    public static SettlementRank valueOf(String string) throws IllegalArgumentException {
        SettlementRank rank = get(string);
        if (rank == null) {
            throw new IllegalArgumentException("No SettlementRank with name " + string);
        }
        return rank;
    }

    public List<String> getTitles(Settlement settlement) {
        return settlement != null ? getTitles(settlement.getState()) : new ArrayList<>();
    }

    public String getTag(Settlement landOwner) {
        String title = landOwner != null ? landOwner.getDefaultTitle(this) : "";
        return title.isEmpty() ? getTag() : title;
    }

    public List<String> getTitles(SettlementState state) {
        return (List<String>) titles.get(state);
    }

    public static SettlementRank[] values() {
        return RankManager.getInstance().getSettlementRanks().toArray(new SettlementRank[0]);
    }

    @Override
    public SettlementRank[] getValues() {
        return values();
    }

    private void addPermission(String permName) {
        permissions.add(permName);
        if (Bukkit.getPluginManager().getPermission("settlements.command." + permName) == null) {
            Bukkit.getPluginManager().addPermission(new Permission(
                    "settlements.command." + permName,
                    Message.orElse("command." + permName + ".permission.description", Message.get("command." + permName + ".description")).getText(),
                    PermissionDefault.TRUE
            ));
        }
    }

    public LinkedHashSet<String> getPermissions() {
        return permissions;
    }

    public Set<String> getInheritedPermissions() {
        Set<String> inheritedPerms = new LinkedHashSet<>(permissions);
        if (getInherits() != null) {
            inheritedPerms.addAll(getInheritedRank().getInheritedPermissions());
        }
        return inheritedPerms;
    }

    public boolean getPermissionValue(String permission) {
        return permissions.contains(permission);
    }

    public Optional<Boolean> getInheritedPermissionValue(String permission) {
        boolean value = getPermissionValue(permission);
        if (value) {
            return Optional.of(true);
        } else if (getInherits() == null) {
            return Optional.empty();
        }
        return getInheritedRank().getInheritedPermissionValue(permission);
    }

    public String getInherits() {
        return inherits;
    }

    public void setInherits(String inherits) {
        this.inherits = inherits;
    }

    public Collection<SettlementRank> getParentRanks() {
        List<SettlementRank> ranks = new ArrayList<>();
        SettlementRank rank = this;
        while (rank.getParent() != null) {
            ranks.add(rank.getParentRank());
            rank = rank.getParentRank();
        }
        return ranks;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public String name() {
        return name;
    }

    public int ordinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public int compareTo(SettlementRank o) {
        return Integer.compare(ordinal(), o.ordinal());
    }

}
