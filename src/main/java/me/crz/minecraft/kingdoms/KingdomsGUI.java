package me.crz.minecraft.kingdoms;

import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class KingdomsGUI implements ConfigMessages {
    protected static final ItemStack BORDER_ITEM = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
    protected static final ClickableItem BORDER = new ClickableItem() {
        @Override
        public ItemStack getItem(Player p0) {
            return BORDER_ITEM;
        }

        @Override
        public boolean click(Player p0, ClickType p1) {
            return false;
        }
    };

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
