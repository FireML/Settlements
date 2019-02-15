package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.NameTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class Rank implements Rankable, NameTag, Comparable<Rank> {

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
                String permName = permission.toLowerCase();
                permissions.add(permName);
                if (Bukkit.getPluginManager().getPermission("kingdoms.command." + permName) == null) {
                    Bukkit.getPluginManager().addPermission(new Permission(
                            "kingdoms.command." + permName,
                            Message.get("command." + permName).getText(),
                            PermissionDefault.TRUE
                    ));
                }
            }
            if (config.isString("icon")) {
                icon = Material.matchMaterial(config.getString("icon"));
                if (icon == null) {
                    icon = Material.WOODEN_SHOVEL;
                }
            }
            if (config.isString("inherits")) {
                inherits = config.getString("inherits");
            }
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

    @Override
    public String name() {
        return name;
    }

    @Override
    public int ordinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public CItem getItem() {
        return new CItem(icon).hideAllFlags(true);
    }

    @Override
    public int compareTo(Rank o) {
        return Integer.compare(ordinal(), o.ordinal());
    }
}
