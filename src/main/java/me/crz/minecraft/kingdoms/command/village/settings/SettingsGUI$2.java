package me.crz.minecraft.kingdoms.command.village.settings;

import me.crz.minecraft.kingdoms.landflags.UseGui;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class SettingsGUI$2 extends ClickableItem {
    final /* synthetic */ SettingsGUI this$0;
    
    SettingsGUI$2(final SettingsGUI this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        return new CItem(Material.GRASS).setName("&b&lUse").build();
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        UseGui.getInstance().open(p);
        return true;
    }
}