package me.crz.minecraft.kingdoms.teleporter;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class TeleportersGUI$2 extends ClickableItem {
    final /* synthetic */ TeleportersGUI this$0;
    
    TeleportersGUI$2(final TeleportersGUI this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        return new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15);
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        return false;
    }
}