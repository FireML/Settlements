package me.crz.minecraft.kingdoms.teleporter;

import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import org.bukkit.inventory.ItemStack;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PrivateTeleportersGUI extends KingdomsGUI {
    private long updated = 0;
    private List<ClickableItem> items = new ArrayList<>();

    public PrivateTeleportersGUI(Village village) {
        super("private-teleports");
        update(village);
    }

    public void open(Player p, int page) {
        update(VillageManager.getInstance().getVillageFromPlayer(p));
        CInventory ci = new CInventory(getMessage("title").getReplacedText("page", String.valueOf(page)), InventorySize.FOUR) {};
        for (int j = 18 * (page - 1); j < 18 * page && j < items.size(); ++j) {
            ci.addItem(items.get(j));
        }
        if (page > 1) {
            ci.addItem(18, new ClickableItem() {
                @Override
                public ItemStack getItem(Player player) {
                    return new CItem(Material.PAPER)
                            .setName(getMessage("page.previous").getReplacedText("page", String.valueOf(page - 1)))
                            .build();
                }

                @Override
                public boolean click(Player player, ClickType type) {
                    open(p, page - 1);
                    return true;
                }
            });
        }
        if (page * 18 < items.size()) {
            ci.addItem(26, new ClickableItem() {
                @Override
                public ItemStack getItem(Player player) {
                    return new CItem(Material.PAPER)
                            .setName(getMessage("page.next").getReplacedText("page", String.valueOf(page + 1)))
                            .build();
                }

                @Override
                public boolean click(Player player, ClickType type) {
                    open(p, page + 1);
                    return true;
                }
            });
        }
        ci.addItem(22, new ClickableItem() {
            @Override
            public ItemStack getItem(Player p0) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            @Override
            public boolean click(Player p, ClickType type) {
                TeleportersGUI.getInstance().build(p);
                return true;
            }
        });
        ci.build(p);
    }

    public void update(Village village) {
        if (updated + 60 * 1000 > System.currentTimeMillis()) {
            return;
        }
        updated = System.currentTimeMillis();
        Set<LandOwner> entries = new LinkedHashSet<>();
        village.getAllies().stream()
                .filter(v -> v.getAccess().atLeast(LandAccess.ALLY))
                .forEachOrdered(entries::add);

        if (village.hasEmpire()) {
            village.getEmpire().getMembers().stream()
                    .filter(v -> !v.equals(village) && v.getAccess().atLeast(LandAccess.TRUSTED))
                    .sorted(Village::compareTo)
                    .forEachOrdered(entries::add);
        }

        EmpireManager.getInstance().getEmpires().stream()
                .filter(e -> !e.equals(village.getEmpire()) && e.getAccess().atLeast(LandAccess.PUBLIC))
                .sorted(Empire::compareTo)
                .forEachOrdered(entries::add);

        VillageManager.getInstance().getVillages().stream()
                .filter(v -> !v.equals(village) && v.getAccess().atLeast(LandAccess.PUBLIC))
                .sorted(Village::compareTo)
                .forEachOrdered(entries::add);

        items = entries.stream().map(landOwner -> new ClickableItem() {
            public ItemStack getItem(Player p) {
                return Utils.getHead(landOwner.getChiefId(), getMessage("entry").getReplacedText("name", landOwner.getName(), "state", landOwner.getStateTag()));
            }

            public boolean click(Player p, ClickType clickType) {
                if (landOwner.getHome() != null) {
                    p.teleportAsync(landOwner.getHome().toBukkitLocation());
                    return true;
                }
                return false;
            }
        }).collect(Collectors.toList());
    }
}
