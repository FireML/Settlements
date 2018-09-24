package me.crz.minecraft.kingdoms.teleporter;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import me.crz.minecraft.kingdoms.Base;
import org.bukkit.Location;
import org.bson.Document;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;
import java.util.List;
import java.util.Arrays;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class PrivateTeleportersGUI$2 extends ClickableItem {
    final /* synthetic */ PrivateTeleportersGUI this$0;
    private final /* synthetic */ Village val$v;
    
    PrivateTeleportersGUI$2(final PrivateTeleportersGUI this$0, final Village val$v) {
        this$0 = this$0;
        val$v = val$v;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        final ItemStack item = new ItemStack(Material.SKULL_ITEM);
        final SkullMeta sm = (SkullMeta)item.getItemMeta();
        sm.setDisplayName(MessageUtils.replaceColorCodes("&b" + val$v.getName()));
        sm.setLore((List)Arrays.<String>asList(MessageUtils.replaceColorCodes("&a&lCost: &r&6" + val$v.getTeleportCost() + "&r&7$")));
        sm.setOwningPlayer(Bukkit.getOfflinePlayer(val$v.getChief()));
        sm.setOwner(Bukkit.getOfflinePlayer(val$v.getChief()).getName());
        item.setItemMeta((ItemMeta)sm);
        return item;
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        final Document d = (Document)val$v.getDocument().get((Object)"teleporterClaim");
        final Location location = new Location(Bukkit.getWorld(d.getString((Object)"worldName")), d.getInteger((Object)"x"), d.getInteger((Object)"y"), (double)d.getInteger((Object)"z"));
        if (val$v.getTeleportCost() == 0) {
            p.teleport(location);
        }
        else {
            final OfflinePlayer op = Bukkit.getOfflinePlayer(p.getUniqueId());
            if (Base.getEconomy().getBalance(op) >= val$v.getTeleportCost()) {
                final EconomyResponse r = Base.getEconomy().withdrawPlayer(op, (double)val$v.getTeleportCost());
                if (r.transactionSuccess()) {
                    p.teleport(location);
                    MessageUtils.tell(p, "&7Teleporting to &6" + val$v.getName() + "&r&7's teleporter!", null, null);
                    MessageUtils.tellResidents(val$v.getResidents(), "&e&l" + p.getName() + " &r&7deposited &6" + val$v.getTeleportCost() + "&r&7 into the village bank by teleporting to your teleporter!", null, null);
                }
                else {
                    MessageUtils.tell(p, "&cAn error occurred, please tell an admin!", null, null);
                }
            }
            else {
                MessageUtils.tell(p, "&c&lNot enough money in your bank to pay &r&6&l" + val$v.getTeleportCost() + "&r&c&l to teleport!", null, null);
            }
        }
        return false;
    }
}