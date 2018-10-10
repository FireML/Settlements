package me.crz.minecraft.kingdoms.teleporter;

import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import net.milkbowl.vault.economy.EconomyResponse;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import me.crz.minecraft.kingdoms.Base;

public class PrivateTeleportersGUI extends Base {
    private Set<IdObject> allies = new LinkedHashSet<>();
    private int checks = 0;
    private List<ClickableItem> items = new ArrayList<>();

    public PrivateTeleportersGUI(Village village) {
        super();
        this.update(village);
    }

    public void open(Player p, int i) {
        this.update(VillageManager.getInstance().getVillageFromPlayer(p));
        CInventory ci = new CInventory("Private Warps: " + (i + 1), InventorySize.FOUR) {};
        if (items.isEmpty()) {
            return;
        }
        if (items.size() >= 14 * (i + 1)) {
            for (int j = 14 * i; j < 14 * (i + 1); ++j) {
                ci.addItem(items.get(j));
            }
        } else {
            for (ClickableItem item : items.subList(14 * i, items.size())) {
                ci.addItem(item);
            }
        }
        CInventoryManager.getInstance().addCInvetory(ci);
        ci.build(p);
    }

    public void update(Village village) {
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
        for (IdObject ally : allies) {
            Village v = VillageManager.getInstance().getVillageFromId(ally);
            if (v == null || v.getAccess().atLeast(VillageAccess.ALLY)) {
                continue;
            }
            items.add(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta sm = (SkullMeta) item.getItemMeta();
                    sm.setDisplayName(MessageUtils.replaceColorCodes("&b" + v.getName()));
                    sm.setOwningPlayer(Bukkit.getOfflinePlayer(v.getChief()));
                    item.setItemMeta(sm);
                    return item;
                }

                public boolean click(Player p, ClickType clickType) {
                    p.teleport(v.getVillageHome().toBukkitLocation());
                    return false;
                }
            });
        }
    }
}
