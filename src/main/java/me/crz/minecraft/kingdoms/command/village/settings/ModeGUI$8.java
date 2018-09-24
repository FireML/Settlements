package me.crz.minecraft.kingdoms.command.village.settings;

import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class ModeGUI$8 extends ClickableItem {
    final /* synthetic */ ModeGUI this$0;
    
    ModeGUI$8(final ModeGUI this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        return new CItem(Material.BARRIER).setName("&c&lBack To Settings").build();
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        SettingsGUI.getInstance().open(p);
        return true;
    }
}