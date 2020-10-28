package me.crz.minecraft.kingdoms;

import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class KingdomsGUI implements ConfigMessages {
    protected static final ItemStack BORDER_ITEM = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

    private final String name;

    public KingdomsGUI(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public String type() {
        return "gui";
    }
}
