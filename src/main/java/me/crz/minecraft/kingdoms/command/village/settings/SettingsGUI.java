package me.crz.minecraft.kingdoms.command.village.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.landflags.UseGui;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.KingdomsGUI;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SettingsGUI extends KingdomsGUI {
    private static SettingsGUI instance;
    public CInventory gui;

    private SettingsGUI() {
        super("settings");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.IRON_DOOR).setName(getText("access")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                AccessGUI.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.DIAMOND_PICKAXE).setName(getText("build")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                BuildGui.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.LEVER).setName(getText("use")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                UseGui.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.REDSTONE).setName(getText("mode")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                ModeGUI.getInstance().open(p);
                return true;
            }
        });
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.LINGERING_POTION).setName(getText("perks")).build();
            }

            public boolean click(Player p, ClickType clickType) {
                new PerksGUI(ResidentManager.getInstance().getResident(p)).open();
                return true;
            }
        });
        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(Player p) {
                Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                if (village == null) {
                    return null;
                }
                return new CItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(
                        getMessage("entry").getReplacedText(village.getReplacements(instance))
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

    public static SettingsGUI getInstance() {
        if (instance == null) {
            instance = new SettingsGUI();
        }
        return instance;
    }
}
