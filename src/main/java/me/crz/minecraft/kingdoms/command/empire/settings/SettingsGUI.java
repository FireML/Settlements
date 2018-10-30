package me.crz.minecraft.kingdoms.command.empire.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.command.village.settings.BuildGui;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.KingdomsGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SettingsGUI extends KingdomsGUI {
    private static SettingsGUI instance;
    public CInventory gui;

    private SettingsGUI() {
        super("empire-settings");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.DIAMOND_PICKAXE).setName(getText("build")).hideAllFlags(true).build();
            }

            public boolean click(Player p, ClickType clickType) {
                BuildGui.getInstance().open(p);
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
        gui.addItem(new ClickableItem() {
            public ItemStack getItem(Player p) {
                return new CItem(Material.LINGERING_POTION).setName(getText("perks")).hideAllFlags(true).build();
            }

            public boolean click(Player p, ClickType clickType) {
                new PerksGUI(ResidentManager.getInstance().getResident(p)).open();
                return true;
            }
        });
        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(Player p) {
                Resident resident = ResidentManager.getInstance().getResident(p);
                if (!resident.hasEmpire()) {
                    return null;
                }
                Empire empire = resident.getVillage().getEmpire();
                return new CItem(Material.BLUE_BED).setName(
                        getMessage("entry").getReplacedText(empire.getReplacements(instance))
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
