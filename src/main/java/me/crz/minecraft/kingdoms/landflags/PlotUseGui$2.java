package me.crz.minecraft.kingdoms.landflags;

import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class PlotUseGui$2 extends ClickableItem {
    final /* synthetic */ PlotUseGui this$0;
    
    PlotUseGui$2(final PlotUseGui this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        return new CItem(Material.STAINED_GLASS_PANE, (byte)15).build();
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        return false;
    }
}