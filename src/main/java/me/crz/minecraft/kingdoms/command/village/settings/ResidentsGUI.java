package me.crz.minecraft.kingdoms.command.village.settings;

import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;

import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import net.md_5.bungee.api.ChatColor;
import me.crz.minecraft.kingdoms.cinventory.CInventory;

public class ResidentsGUI {
    public CInventory gui;

    public static String cfc(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        return String.valueOf(string.substring(0, 1).toUpperCase()) + string.substring(1, string.length());
    }

    public static String c(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public ResidentsGUI() {
        super();
        (gui = new CInventory("&3&lVillage Residents", InventorySize.SIX) {}).addItem(53, new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.BARRIER).setName("&c&lBack To Settings").build();
            }

            public boolean click(Player p, ClickType clickType) {
                SettingsGUI.getInstance().open(p);
                return true;
            }
        });
        CInventoryManager.getInstance().addCInvetory(gui);
    }

    public void open(Player p) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(p);
        gui.clearItems();
        int i = 0;
        for (Resident res : village.getResidents()) {
            if (i > 52) {
                break;
            }
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(res.getUUID());
                    return new CItem(Material.PLAYER_HEAD).setName("&c&l" + op.getName()).setLore(Arrays.asList(ResidentsGUI.c("&7&lRole"), ResidentsGUI.c("&b&l" + ResidentsGUI.cfc(res.getVillageRank().name())))).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    return true;
                }
            });
            ++i;
        }
        gui.build(p);
    }
}
