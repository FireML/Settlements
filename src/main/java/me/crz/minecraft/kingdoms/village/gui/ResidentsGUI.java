package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;

import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import net.md_5.bungee.api.ChatColor;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import org.bukkit.inventory.meta.SkullMeta;

public class ResidentsGUI extends KingdomsGUI {

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
        super("residents");
    }

    public void open(Player p) {
        Utils.runAsync(() -> {
            CInventory gui = new CInventory(getText("title"), InventorySize.SIX) {};
            gui.addItem(53, new ClickableItem() {
                public ItemStack getItem(Player p) {
                    return new CItem(Material.BARRIER).setName(getText("back")).build();
                }

                public boolean click(Player p, ClickType clickType) {
                    new SettingsGUI(p).open();
                    return true;
                }
            });
            Village village = VillageManager.getInstance().getVillageFromPlayer(p);
            int i = 0;
            for (Resident res : village.getResidents()) {
                if (i > 52) {
                    break;
                }
                gui.addItem(new ClickableItem() {
                    public ItemStack getItem(Player p) {
                        ItemStack item = new CItem(Material.PLAYER_HEAD)
                                .setName(getMessage("entry").getReplacedText(
                                        "name", res.getName(),
                                        "rank", res.getVillageRank().getTag(village),
                                        "title", res.getTitle(),
                                        "joined", Utils.TIME_FORMAT.format(res.getVillageJoin()),
                                        "lastlogin", Utils.TIME_FORMAT.format(res.getLastLogin())
                                ))
                                .build();
                        SkullMeta meta = (SkullMeta) item.getItemMeta();
                        meta.setPlayerProfile(Bukkit.createProfile(res.getUUID(), res.getName()));
                        item.setItemMeta(meta);
                        return item;
                    }

                    public boolean click(Player p, ClickType clickType) {
                        return true;
                    }
                });
                ++i;
            }
            gui.build(p);
        });
    }
}
