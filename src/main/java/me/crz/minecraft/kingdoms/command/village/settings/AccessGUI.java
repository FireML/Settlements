package me.crz.minecraft.kingdoms.command.village.settings;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import me.crz.minecraft.kingdoms.village.VillageGUI;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class AccessGUI extends VillageGUI {
    private static AccessGUI instance;
    private CInventory gui;

    public AccessGUI() {
        super("access");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        gui.addItem(8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                SettingsGUI.getInstance().open(p);
                return true;
            }
        });
        for (VillageAccess access : VillageAccess.values()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    Message description = getMessage("access." + access.getKey());
                    if (village != null && village.getAccess() == access) {
                        return new CItem(Material.LIME_WOOL)
                                .setName(getMessage("selected")
                                        .getReplacedText("access", access.getTag()))
                                .setLore(description.getTextList())
                                .build();
                    }
                    return new CItem(Material.LIGHT_GRAY_WOOL)
                            .setName(getMessage("available")
                                    .getReplacedText("access", access.getTag()))
                            .setLore(description.getTextList())
                            .build();
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    Resident resident = ResidentManager.getInstance().getResident(p);
                    if (resident.getVillage() != null && resident.getVillageRank().atLeast(VillageRank.CHIEF)) {
                        if (resident.getVillage().getAccess() != access) {
                            resident.getVillage().setAccess(access);
                            resident.tell(Message.get("commands.village.setaccess.success"), "access", access.getTag());
                            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
                            gui.build(p);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        CInventoryManager.getInstance().addCInvetory(gui);
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static AccessGUI getInstance() {
        if (instance == null) {
            instance = new AccessGUI();
        }
        return instance;
    }
}
