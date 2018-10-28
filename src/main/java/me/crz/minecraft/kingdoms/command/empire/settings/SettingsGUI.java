package me.crz.minecraft.kingdoms.command.empire.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SettingsGUI extends VillageGUI {
    private static SettingsGUI instance;
    public CInventory gui;

    public SettingsGUI() {
        super("settings");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
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
                Resident resident = ResidentManager.getInstance().getResident(p);
                if (!resident.hasEmpire()) {
                    return null;
                }
                Empire empire = resident.getVillage().getEmpire();
                Village capital = empire.getCapital();
                Resident chief = ResidentManager.getInstance().getResident(capital.getChief());
                return new CItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(
                        getMessage("entry").getReplacedText(
                                "name", empire.getName(),
                                "level", String.valueOf(empire.getLevel()),
                                "bank", String.valueOf(empire.getBank()),
                                "bonusClaims", String.valueOf(empire.getBonusClaims()),
                                "bonus", empire.getBonusClaims() > 0 ? getMessage("bonus").getReplacedText("amount", String.valueOf(empire.getBonusClaims())) : "",
                                "members", String.valueOf(empire.getMemberIds().size()),
                                "capital", capital.getName(),
                                "capitalstate", capital.getState().getTag(),
                                "chiefRank", VillageRank.CHIEF.getTag(capital),
                                "chiefTitle", chief.getName(),
                                "chief", chief.getName()
                        )
                ).build();
            }

            public boolean click(Player p, ClickType clickType) {
                return false;
            }
        });
        CInventoryManager.getInstance().addCInvetory(gui);
    }

    public void open(Player p) {
        Utils.runAsync(() -> gui.build(p));
    }

    public static SettingsGUI getInstance() {
        if (instance == null) {
            instance = new SettingsGUI();
        }
        return instance;
    }
}
