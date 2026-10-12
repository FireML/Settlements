package de.themoep.settlements.rank;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.utils.ItemUtils;
import de.themoep.settlements.utils.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashSet;
import java.util.Set;

public class PermissionGroup {
    private final String name;
    private final Material icon;

    private final Set<String> permissions = new LinkedHashSet<>();

    public PermissionGroup(ConfigurationSection config) throws InvalidConfigurationException {
        this.name = config.getName();
        if (config.isString("icon")) {
            Material icon = MaterialUtils.getMaterial(config.getString("icon"));
            if (icon != null && !icon.isItem()) {
                SettlementsPlugin.getInstance().getLogging().warn(icon + " is not a valid icon type for permission group " + name + "! That type has no item for the GUI!");
                icon = Material.DIRT;
            }
            this.icon = icon;
        } else {
            this.icon = null;
        }
        for (String perm : config.getStringList("permissions")) {
            permissions.add(perm.toLowerCase());
        }
        if (permissions.isEmpty()) {
            throw new InvalidConfigurationException("No permissions defined!");
        }
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(icon != null ? icon : Material.DIRT);
        ItemUtils.hideAllFlags(item);
        return item;
    }

    public Set<String> getPermissions() {
        return permissions;
    }
}
