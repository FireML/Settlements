package de.themoep.settlements;

import de.themoep.settlements.utils.ConfigMessages;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class SettlementsGUI implements ConfigMessages {
    protected static final ItemStack BORDER_ITEM = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

    private final String name;

    public SettlementsGUI(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String type() {
        return "gui";
    }
}
