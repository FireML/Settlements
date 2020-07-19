package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class AccessGUI extends KingdomsGUI {
    private static AccessGUI instance;
    private CInventory gui;

    private AccessGUI() {
        super("access");
        gui = new CInventory(getText("title"), InventorySize.SIX) {};
        for (int i = 0; i < InventorySize.TWO.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        for (LandAccess access : LandAccess.values()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    if (village != null) {
                        Message description = getMessage("access." + access.getKey());
                        if (village.getAccess() == access) {
                            return new CItem(Material.LIME_WOOL)
                                    .setName(getMessage("selected")
                                            .getReplacedText("access", access.getTag()))
                                    .setLore(description.getReplacedTextList("state", village.getStateTag()))
                                    .build();
                        }
                        return new CItem(Material.LIGHT_GRAY_WOOL)
                                .setName(getMessage("available")
                                        .getReplacedText("access", access.getTag()))
                                .setLore(description.getReplacedTextList("state", village.getStateTag()))
                                .build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    if (resident.hasVillage() && resident.hasPermission("village.setaccess")) {
                        if (resident.getVillage().getAccess() != access) {
                            resident.getVillage().setAccess(access);
                            resident.tell(Message.get("command.village.setaccess.success"), "access", access.getTag());
                            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
                            gui.build(p);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        for (int i = InventorySize.FOUR.getSize(); i < InventorySize.SIX.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        gui.addItem(InventorySize.FIVE.getSize() + 4, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                new SettingsGUI(p).open();
                return true;
            }
        });
    }

    public void open(final Player p) {
        gui.build(p);
    }

    synchronized public static AccessGUI getInstance() {
        if (instance == null) {
            instance = new AccessGUI();
        }
        return instance;
    }
}
