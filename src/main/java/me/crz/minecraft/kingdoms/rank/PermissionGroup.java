package me.crz.minecraft.kingdoms.rank;

import me.crz.minecraft.kingdoms.utils.ItemUtils;
import me.crz.minecraft.kingdoms.utils.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashSet;
import java.util.Set;

public class PermissionGroup {
    private final String name;
    private final Material icon;

    private final Set<String> permissions = new LinkedHashSet<>();

    public PermissionGroup(ConfigurationSection config) {
        this.name = config.getName();
        this.icon = MaterialUtils.getMaterial(config.getString("icon"));
        for (String perm : config.getStringList("permissions")) {
            permissions.add(perm.toLowerCase());
        }
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return ItemUtils.hideAllFlags(new ItemStack(icon != null ? icon : Material.DIRT));
    }

    public Set<String> getPermissions() {
        return permissions;
    }
}
