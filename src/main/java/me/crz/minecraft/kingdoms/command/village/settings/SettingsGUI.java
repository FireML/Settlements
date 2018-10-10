package me.crz.minecraft.kingdoms.command.village.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import org.bukkit.OfflinePlayer;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.landflags.UseGui;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;

import java.util.HashMap;

import me.crz.minecraft.kingdoms.cinventory.CInventory;

import java.util.UUID;
import java.util.Map;

public class SettingsGUI {
    private static SettingsGUI instance;
    private static Map<UUID, ResidentsGUI> guis;
    public CInventory gui;

    static {
        guis = new HashMap<>();
    }

    public SettingsGUI() {
        super();
        gui = new CInventory("&3&lSettlement Settings", InventorySize.ONE) {};
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.GRASS).setName("&b&lAccess").build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                AccessGUI.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.GRASS).setName("&b&lUse").build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                UseGui.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.REDSTONE).setName("&b&lMode").build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                ModeGUI.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                final StringBuilder sb = new StringBuilder("&8--------&c&l");
                sb.append(village.getName());
                sb.append("&r&8--------\n");
                sb.append("&a&lState &r&7");
                sb.append(village.getState().humanizedName());
                sb.append("\n&6&lLevel &r&7");
                sb.append(village.getLevel());
                sb.append("\n&6&lAccess &r&7");
                sb.append(village.getAccess().humanizedName());
                sb.append("\n&6&lLand &r&7");
                sb.append(LandManager.getInstance().getClaimedLandSize(village));
                sb.append("&r&e/&r&7");
                sb.append(Utils.getChunkMax(village.getLevel(), village.getState()));
                sb.append("\n&e&l");
                sb.append(VillageRank.CHIEF.getTag(village.getState()));
                sb.append(" &r&7");
                final OfflinePlayer player = Bukkit.getOfflinePlayer(village.getChief());
                sb.append(player.getName());
                return new CItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName("&b&lVillage Data").setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', sb.toString()).split("\n"))).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                return false;
            }
        });
        CInventoryManager.getInstance().addCInvetory(gui);
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static ResidentsGUI getGUI(final Player p) {
        if (!guis.containsKey(p.getUniqueId())) {
            final ResidentsGUI instance = new ResidentsGUI();
            guis.put(p.getUniqueId(), instance);
            return instance;
        }
        return guis.get(p.getUniqueId());
    }

    public static SettingsGUI getInstance() {
        if (instance == null) {
            instance = new SettingsGUI();
        }
        return instance;
    }
}
