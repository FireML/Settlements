package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.rank.PermissionGroup;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.IdObject;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RankPermGroupsGUI extends KingdomsGUI {
    private static Map<String, Map<IdObject, RankPermGroupsGUI>> instances = new ConcurrentHashMap<>();
    public CInventory gui;

    private RankPermGroupsGUI(Village village, VillageRank rank) {
        super("rank-perm-groups");
        gui = new CInventory(getText("title", "rank", rank.getTag(village)), InventorySize.ONE) {};

        for (PermissionGroup group : VillageRank.getPermGroups()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasVillage()) {
                        return group.getItem().setName(getMessage("groups." + group.getName()).getReplacedText(
                                "rank", rank.getTag(),
                                "state", res.getVillage().getStateTag()
                        )).build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType type) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasVillage() && res.hasPermission("village.perms")) {
                        RankPermsGUI.getInstance(village, rank, group).open(p);
                        return true;
                    }
                    return false;
                }
            });
        }
        gui.addItem((gui.getMaxSlot() / 9) * 9 + 8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                PermsGUI.getInstance().open(p);
                return true;
            }
        });
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static void open(Resident resident, VillageRank rank) {
        getInstance(resident.getVillage(), rank).open(resident.getPlayer());
    }

    public static RankPermGroupsGUI getInstance(Village village, VillageRank rank) {
        return instances
                .computeIfAbsent(rank.name(), n -> new ConcurrentHashMap<>())
                .computeIfAbsent(village.getId(), id -> new RankPermGroupsGUI(village, rank));
    }
}
