package de.themoep.settlements.rank;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.utils.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import uk.firedev.poleislib.Loggers;
import uk.firedev.poleislib.utils.ItemUtils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

public class PermissionGroup {
    private final String name;
    private final Material icon;

    private final Set<String> permissions = new LinkedHashSet<>();

    public PermissionGroup(ConfigurationSection config) throws InvalidConfigurationException {
        this.name = config.getName();
        if (config.isString("icon")) {
            Material icon = MaterialUtils.getMaterial(config.getString("icon"));
            if (icon != null && !icon.isItem()) {
                Loggers.log(Level.WARNING, SettlementsPlugin.getInstance().getLogger(), icon + " is not a valid icon type for permission group " + name + "! That type has no item for the GUI!");
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
        return ItemUtils.hideAllFlags(new ItemStack(icon != null ? icon : Material.DIRT));
    }

    public Set<String> getPermissions() {
        return permissions;
    }
}
