package me.crz.minecraft.kingdoms.teleporter;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.Location;
import org.bson.Document;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.Bukkit;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

import java.util.Collections;
import java.util.List;

import me.crz.minecraft.kingdoms.Base;

public class PrivateTeleportersGUI extends Base {
    private List<String> allies = new ArrayList<>();
    private int checks = 0;
    private List<ClickableItem> items = new ArrayList<>();

    public PrivateTeleportersGUI(final Village village) {
        super();
        this.update(village);
    }

    public void open(final Player p, final int i) {
        this.update(VillageManager.getInstance().getVillageFromPlayer(p));
        final CInventory ci = new CInventory("Private Teleporters: " + (i + 1), InventorySize.FOUR) {};
        if (items.isEmpty()) {
            return;
        }
        if (items.size() >= 14 * (i + 1)) {
            for (int j = 14 * i; j < 14 * (i + 1); ++j) {
                ci.addItem(items.get(j));
            }
        } else {
            for (final ClickableItem item : items.subList(14 * i, items.size())) {
                ci.addItem(item);
            }
        }
        CInventoryManager.getInstance().addCInvetory(ci);
        ci.build(p);
    }

    public void update(final Village village) {
        if (village == null) {
            allies.clear();
            return;
        }
        if (village.getAllies() == allies && checks < 2) {
            ++checks;
            return;
        }
        allies = village.getAllies();
        checks = 0;
        for (final String ally : allies) {
            final Village v = VillageManager.getInstance().getVillageFromName(ally);
            if (v == null) {
                continue;
            }
            if (v.getTeleporterClaim() == null) {
                continue;
            }
            items.add(new ClickableItem() {
                public ItemStack getItem(final Player p) {
                    final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    final SkullMeta sm = (SkullMeta) item.getItemMeta();
                    sm.setDisplayName(MessageUtils.replaceColorCodes("&b" + v.getName()));
                    sm.setLore(Collections.singletonList(MessageUtils.replaceColorCodes("&a&lCost: &r&6" + v.getTeleportCost() + "&r&7$")));
                    sm.setOwningPlayer(Bukkit.getOfflinePlayer(v.getChief()));
                    item.setItemMeta(sm);
                    return item;
                }

                public boolean click(final Player p, final ClickType clickType) {
                    final Document d = (Document) v.getDocument().get("teleporterClaim");
                    final Location location = new Location(Bukkit.getWorld(d.getString("worldName")), d.getInteger("x"), d.getInteger("y"), (double) d.getInteger("z"));
                    if (v.getTeleportCost() == 0) {
                        p.teleport(location);
                    } else {
                        final OfflinePlayer op = Bukkit.getOfflinePlayer(p.getUniqueId());
                        if (Base.getEconomy().getBalance(op) >= v.getTeleportCost()) {
                            final EconomyResponse r = Base.getEconomy().withdrawPlayer(op, (double) v.getTeleportCost());
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
        }
    }
}
