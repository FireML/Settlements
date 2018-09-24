package me.crz.minecraft.kingdoms.teleporter;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class TeleportersGUI$3 extends ClickableItem {
    final /* synthetic */ TeleportersGUI this$0;
    
    TeleportersGUI$3(final TeleportersGUI this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        final ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)13);
        final ItemMeta pm = item.getItemMeta();
        pm.setDisplayName(MessageUtils.replaceColorCodes("&e&lPrivate Warps"));
        item.setItemMeta(pm);
        return item;
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        final Village v = VillageManager.getInstance().getVillageFromPlayer(p);
        if (v == null) {
            return false;
        }
        Teleporters.getInstance().getPrivateTeleporters(v, p).open(p, 0);
        return true;
    }
}