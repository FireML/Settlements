package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.command.village.settings.SettingsGUI;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class UseGui$2 extends ClickableItem {
    final /* synthetic */ UseGui this$0;
    private final /* synthetic */ int val$i;
    
    UseGui$2(final UseGui this$0, final int val$i) {
        this$0 = this$0;
        val$i = val$i;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        if (val$i != 35) {
            return new CItem(Material.STAINED_GLASS_PANE, (byte)15).build();
        }
        return new CItem(Material.BARRIER).setName("&c&lBack To Settings").build();
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        if (val$i == 35) {
            SettingsGUI.getInstance().open(p);
            return true;
        }
        return false;
    }
}