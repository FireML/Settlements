package de.themoep.settlements.rank;

import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.MaterialUtils;
import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

public class PermissionGroup {
    private final String name;
    private final Material icon;

    private final Set<String> permissions = new LinkedHashSet<>();

    public PermissionGroup(ConfigurationSection config) {
        this.name = config.getName();
        Material icon = MaterialUtils.getMaterial(config.getString("icon"));
        if (icon != null && !icon.isItem()) {
            SettlementsPlugin.getInstance().getLogger().log(Level.WARNING, icon + " is not a valid icon type for permission group " + name + "! That type has no item for the GUI!");
            icon = Material.DIRT;
        }
        this.icon = icon;
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
