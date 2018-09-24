package me.crz.minecraft.kingdoms.teleporter;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class TeleportersGUI$4 extends ClickableItem {
    final /* synthetic */ TeleportersGUI this$0;
    
    TeleportersGUI$4(final TeleportersGUI this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        final Village v = VillageManager.getInstance().getVillageFromPlayer(p);
        if (v == null || v.getState().equals(VillageState.VILLAGE) || v.getLevel() < 4) {
            MessageUtils.tell(p, "&c&lYou require a level &610 Kingdom&c&l to use this!");
            return locked;
        }
        final ItemStack item = new ItemStack(Material.GRASS);
        final ItemMeta pm = item.getItemMeta();
        pm.setDisplayName(MessageUtils.replaceColorCodes("&e&lKingdoms World"));
        item.setItemMeta(pm);
        return item;
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        final Village v = VillageManager.getInstance().getVillageFromPlayer(p);
        if (v == null || v.getState().equals(VillageState.VILLAGE) || v.getLevel() < 4) {
            MessageUtils.tell(p, "&c&lYou require a level &610 Kingdom&c&l to use this!");
            return false;
        }
        try {
            p.teleport(Bukkit.getWorld("kingdom").getSpawnLocation());
        }
        catch (Exception e) {
            MessageUtils.tell(p, "&c&lAn error occurred somewhere! warp possibly not set!");
        }
        return true;
    }
}