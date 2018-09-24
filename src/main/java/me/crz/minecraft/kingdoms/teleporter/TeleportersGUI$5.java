package me.crz.minecraft.kingdoms.teleporter;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bson.Document;
import me.crz.minecraft.kingdoms.Base;
import org.bukkit.Location;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

class TeleportersGUI$5 extends ClickableItem {
    final /* synthetic */ TeleportersGUI this$0;
    
    TeleportersGUI$5(final TeleportersGUI this$0) {
        this$0 = this$0;
        super();
    }
    
    public ItemStack getItem(final Player p) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        if (village == null) {
            return locked;
        }
        if (village.getTeleporterClaim() == null || village.getTeleporterClaim().getDocument() == null) {
            return locked;
        }
        if (village.getTeleporterClaim().getDocument().getInteger((Object)"x") == null) {
            return locked;
        }
        return Utils.getHead(Bukkit.getOfflinePlayer(village.getChief()), MessageUtils.replaceColorCodes("&e&lVillage Teleporter"));
    }
    
    public boolean click(final Player p, final ClickType clickType) {
        final Village v = VillageManager.getInstance().getVillageFromPlayer(p);
        if (v == null) {
            return false;
        }
        if (v.getTeleporterClaim() == null || v.getTeleporterClaim().getDocument() == null) {
            return false;
        }
        if (v.getTeleporterClaim().getDocument().getInteger((Object)"x") == null) {
            return false;
        }
        final Document d = v.getTeleporterClaim().getDocument();
        final Location location = new Location(Bukkit.getWorld(d.getString((Object)"worldName")), d.getInteger((Object)"x"), d.getInteger((Object)"y"), (double)d.getInteger((Object)"z"));
        if (v.getTeleportCost() == 0) {
            p.teleport(location);
        }
        else {
            final OfflinePlayer op = Bukkit.getOfflinePlayer(p.getUniqueId());
            if (Base.getEconomy().getBalance(op) >= v.getTeleportCost()) {
                final EconomyResponse r = Base.getEconomy().withdrawPlayer(op, (double)v.getTeleportCost());
                if (r.transactionSuccess()) {
                    p.teleport(location);
                    MessageUtils.tell(p, "&7Teleporting to &6" + v.getName() + "&r&7's teleporter!", null, null);
                    MessageUtils.tellResidents(v.getResidents(), "&e&l" + p.getName() + " &r&7deposited &6" + v.getTeleportCost() + "&r&7 into the village bank by teleporting to your teleporter!", null, null);
                }
                else {
                    MessageUtils.tell(p, "&cAn error occurred, please tell an admin!", null, null);
                }
            }
            else {
                MessageUtils.tell(p, "&c&lNot enough money in your bank to pay &r&6&l" + v.getTeleportCost() + "&r&c&l to teleport!", null, null);
            }
        }
        return false;
    }
}