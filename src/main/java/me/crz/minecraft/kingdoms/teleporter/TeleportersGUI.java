package me.crz.minecraft.kingdoms.teleporter;

import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bson.Document;
import org.bukkit.Location;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import org.bukkit.Material;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.inventory.ItemStack;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.Base;

public class TeleportersGUI extends Base
{
    CInventory ci;
    ItemStack locked;
    
    public TeleportersGUI() {
        super();
        ci = new CInventory(MessageUtils.replaceColorCodes("&2&lTeleporters"), InventorySize.ONE) {
            final /* synthetic */ TeleportersGUI this$0;
            
            TeleportersGUI$1(final String $anonymous0, final InventorySize $anonymous1) {
                this$0 = this$0;
                super($anonymous0, $anonymous1);
            }
        };
        locked = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)14);
        final ItemMeta lm = locked.getItemMeta();
        lm.setDisplayName(MessageUtils.replaceColorCodes("Locked"));
        locked.setItemMeta(lm);
        final Integer[] slots = { 0, 1, 2, 6, 7, 8 };
        Integer[] array;
        for (int length = (array = slots).length, j = 0; j < length; ++j) {
            final int i = (int)array[j];
            ci.addItem(i, (ClickableItem)new ClickableItem() {
                final /* synthetic */ TeleportersGUI this$0;
                
                TeleportersGUI$2() {
                    this$0 = this$0;
                    super();
                }
                
                public ItemStack getItem(final Player p) {
                    return new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15);
                }
                
                public boolean click(final Player p, final ClickType clickType) {
                    return false;
                }
            });
        }
        ci.addItem(3, (ClickableItem)new ClickableItem() {
            final /* synthetic */ TeleportersGUI this$0;
            
            TeleportersGUI$3() {
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
        });
        ci.addItem(4, (ClickableItem)new ClickableItem() {
            final /* synthetic */ TeleportersGUI this$0;
            
            TeleportersGUI$4() {
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
        });
        ci.addItem(5, (ClickableItem)new ClickableItem() {
            final /* synthetic */ TeleportersGUI this$0;
            
            TeleportersGUI$5() {
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
        });
        CInventoryManager.getInstance().addCInvetory(ci);
    }
    
    public void build(final Player p) {
        ci.build(p);
    }
}
