package me.crz.minecraft.kingdoms.empire.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class AccessGUI extends KingdomsGUI {
    private static AccessGUI instance;
    private CInventory gui;

    private AccessGUI() {
        super("empire.access");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        for (LandAccess access : LandAccess.values()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Empire empire = EmpireManager.getInstance().getEmpireFromPlayer(p);
                    if (empire != null) {
                        Message description = getMessage("access." + access.getKey());
                        if (empire.getAccess() == access) {
                            return new CItem(Material.LIME_WOOL)
                                    .setName(getMessage("selected")
                                            .getReplacedText("access", access.getTag()))
                                    .setLore(description.getReplacedTextList("state", empire.getStateTag()))
                                    .build();
                        }
                        return new CItem(Material.LIGHT_GRAY_WOOL)
                                .setName(getMessage("available")
                                        .getReplacedText("access", access.getTag()))
                                .setLore(description.getReplacedTextList("state", empire.getStateTag()))
                                .build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    if (resident.hasEmpire() && resident.hasPermission("empire.setaccess")) {
                        if (resident.getVillage().getEmpire().getAccess() != access) {
                            resident.getVillage().getEmpire().setAccess(access);
                            resident.tell(Message.get("command.empire.setaccess.success"), "access", access.getTag());
                            Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire()));
                            gui.build(p);
                            return true;
                        }
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
