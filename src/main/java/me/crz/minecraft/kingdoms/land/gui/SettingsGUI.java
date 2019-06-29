package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SettingsGUI extends PlotGUI {
    private static SettingsGUI instance;
    public CInventory gui;

    private SettingsGUI() {
        super("plot.settings");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.DIAMOND_PICKAXE).setName(getText("build")).hideAllFlags(true).build();
            }

            public boolean click(Player p, ClickType clickType) {
                BuildGui.open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.LEVER).setName(getText("use")).hideAllFlags(true).build();
            }

            public boolean click(Player p, ClickType clickType) {
                UseGui.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.REDSTONE).setName(getText("mode")).hideAllFlags(true).build();
            }

            public boolean click(Player p, ClickType clickType) {
                ModeGUI.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(getPlotLinkItem(gui));
        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(Player p) {
                OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                if (land == null) {
                    return null;
                }

                return new CItem(Material.GREEN_BANNER).setName(
                        getMessage("entry").getReplacedText(land.getReplacements())
                ).build();
            }

            public boolean click(Player p, ClickType clickType) {
                return false;
            }
        });
    }

    public void open(Player p) {
        gui.build(p);
    }

    synchronized public static SettingsGUI getInstance() {
        if (instance == null) {
            instance = new SettingsGUI();
        }
        return instance;
    }
}
