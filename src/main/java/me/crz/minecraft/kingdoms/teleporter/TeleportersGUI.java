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

public class TeleportersGUI extends Base {
    private CInventory ci;
    private ItemStack locked;

    public TeleportersGUI() {
        super();
        ci = new CInventory(MessageUtils.replaceColorCodes("&2&lTeleporters"), InventorySize.ONE) {};
        locked = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta lm = locked.getItemMeta();
        lm.setDisplayName(MessageUtils.replaceColorCodes("Locked"));
        locked.setItemMeta(lm);
        Integer[] slots = {0, 1, 2, 6, 7, 8};
        Integer[] array;
        for (int length = (array = slots).length, j = 0; j < length; ++j) {
            final int i = array[j];
            ci.addItem(i, new ClickableItem() {
                public ItemStack getItem(final Player p) {
                    return new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                }

                public boolean click(final Player p, final ClickType clickType) {
                    return false;
                }
            });
        }
        ci.addItem(3, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta pm = item.getItemMeta();
                pm.setDisplayName(MessageUtils.replaceColorCodes("&e&lPrivate Warps"));
                item.setItemMeta(pm);
                return item;
            }

            public boolean click(final Player p, final ClickType clickType) {
                Village v = VillageManager.getInstance().getVillageFromPlayer(p);
                if (v == null) {
                    return false;
                }
                Teleporters.getInstance().getPrivateTeleporters(v, p).open(p, 0);
                return true;
            }
        });
        ci.addItem(4, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                Village v = VillageManager.getInstance().getVillageFromPlayer(p);
                if (v == null || v.getState().equals(VillageState.VILLAGE) || v.getLevel() < 4) {
                    MessageUtils.tell(p, "&c&lYou require a level &610 Kingdom&c&l to use this!");
                    return locked;
                }
                ItemStack item = new ItemStack(Material.GRASS);
                ItemMeta pm = item.getItemMeta();
                pm.setDisplayName(MessageUtils.replaceColorCodes("&e&lKingdoms World"));
                item.setItemMeta(pm);
                return item;
            }

            public boolean click(final Player p, final ClickType clickType) {
                Village v = VillageManager.getInstance().getVillageFromPlayer(p);
                if (v == null || v.getState().equals(VillageState.VILLAGE) || v.getLevel() < 4) {
                    MessageUtils.tell(p, "&c&lYou require a level &610 Kingdom&c&l to use this!");
                    return false;
                }
                try {
                    p.teleport(Bukkit.getWorld("kingdom").getSpawnLocation());
                } catch (Exception e) {
                    MessageUtils.tell(p, "&c&lAn error occurred somewhere! warp possibly not set!");
                }
                return true;
            }
        });
        ci.addItem(5, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village == null) {
                    return locked;
                }
                if (village.getTeleporterClaim() == null || village.getTeleporterClaim().getDocument() == null) {
                    return locked;
                }
                if (village.getTeleporterClaim().getDocument().getInteger("x") == null) {
                    return locked;
                }
                return Utils.getHead(Bukkit.getOfflinePlayer(village.getChief()), MessageUtils.replaceColorCodes("&e&lVillage Teleporter"));
            }

            public boolean click(final Player p, final ClickType clickType) {
                Village v = VillageManager.getInstance().getVillageFromPlayer(p);
                if (v == null) {
                    return false;
                }
                if (v.getTeleporterClaim() == null || v.getTeleporterClaim().getDocument() == null) {
                    return false;
                }
                if (v.getTeleporterClaim().getDocument().getInteger("x") == null) {
                    return false;
                }
                Document d = v.getTeleporterClaim().getDocument();
                Location location = new Location(Bukkit.getWorld(d.getString("worldName")), d.getInteger("x"), d.getInteger("y"), (double) d.getInteger("z"));
                if (v.getTeleportCost() == 0) {
                    p.teleport(location);
                } else {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(p.getUniqueId());
                    if (Base.getEconomy().getBalance(op) >= v.getTeleportCost()) {
                        EconomyResponse r = Base.getEconomy().withdrawPlayer(op, (double) v.getTeleportCost());
                        if (r.transactionSuccess()) {
                            p.teleport(location);
                            MessageUtils.tell(p, "&7Teleporting to &6" + v.getName() + "&r&7's teleporter!", null, null);
                            MessageUtils.tellResidents(v.getResidents(), "&e&l" + p.getName() + " &r&7deposited &6" + v.getTeleportCost() + "&r&7 into the village bank by teleporting to your teleporter!", null, null);
                        } else {
                            MessageUtils.tell(p, "&cAn error occurred, please tell an admin!", null, null);
                        }
                    } else {
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
