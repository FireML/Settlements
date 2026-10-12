package de.themoep.settlements.rank;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.utils.MaterialUtils;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.utils.Next;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public abstract class Rank<L extends LandOwner, R extends Rankable> implements NameTag, Comparable<R>, Next<R>, Rankable {

    private final String name;

    private int ordinal = Integer.MAX_VALUE;

    private String inherits = null;
    private String parent = null;

    private final LinkedHashSet<String> permissions = new LinkedHashSet<>();
    private Material icon = Material.WOODEN_SHOVEL;

    public Rank(String name, ConfigurationSection config) {
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

    public abstract Rank getInheritedRank();

    public abstract Collection<? extends Rank> getInheritedRanks();

    public String getInherits() {
        return inherits;
    }

    public void setInherits(String inherits) {
        this.inherits = inherits;
    }

    public abstract Rank getParentRank();

    public Collection<Rank> getParentRanks() {
        List<Rank> ranks = new ArrayList<>();
        Rank rank = this;
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

    public abstract List<String> getTitles(L landOwner);

    public String getTag(L landOwner) {
        String title = landOwner != null ? landOwner.getDefaultTitle(this) : "";
        return title.isEmpty() ? getTag() : title;
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
    public int compareTo(Rankable o) {
        return Integer.compare(ordinal(), o.ordinal());
    }
}
