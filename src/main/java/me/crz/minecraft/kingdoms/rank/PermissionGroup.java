package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashSet;
import java.util.Set;

public class PermissionGroup {
    private final String name;
    private final Material icon;

    private final Set<String> permissions = new LinkedHashSet<>();

    public PermissionGroup(ConfigurationSection config) {
        this.name = config.getName();
        this.icon = Material.matchMaterial(config.getString("icon"));
        for (String perm : config.getStringList("permissions")) {
            permissions.add(perm.toLowerCase());
        }
    }

    public String getName() {
        return name;
    }

    public CItem getItem() {
        return new CItem(icon != null ? icon : Material.DIRT).hideAllFlags(true);
    }

    public Set<String> getPermissions() {
        return permissions;
    }
}
