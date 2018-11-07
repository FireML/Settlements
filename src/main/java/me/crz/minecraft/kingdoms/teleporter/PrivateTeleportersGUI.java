package me.crz.minecraft.kingdoms.teleporter;

import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import org.bukkit.event.inventory.ClickType;

import org.bukkit.inventory.ItemStack;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

import java.util.List;

public class PrivateTeleportersGUI extends KingdomsGUI {
    private int checks = 0;
    private List<ClickableItem> items = new ArrayList<>();

    public PrivateTeleportersGUI(Village village) {
        super("private-teleports");
        this.update(village);
    }

    public void open(Player p, int i) {
        this.update(VillageManager.getInstance().getVillageFromPlayer(p));
        CInventory ci = new CInventory(getMessage("title").getReplacedText("page", String.valueOf(i + 1)), InventorySize.FOUR) {};
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
        ci.build(p);
    }

    public void update(Village village) {
        checks = 0;
        for (LandOwner ally : village.getAllies()) {
            if (ally.getAccess().atLeast(LandAccess.ALLY)) {
                continue;
            }
            items.add(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return Utils.getHead(ally.getChiefId(), getMessage("entry").getReplacedText("name", ally.getName(), "state", ally.getStateTag()));
                }

                public boolean click(Player p, ClickType clickType) {
                    p.teleport(ally.getHome().toBukkitLocation());
                    return false;
                }
            });
        }
    }
}
