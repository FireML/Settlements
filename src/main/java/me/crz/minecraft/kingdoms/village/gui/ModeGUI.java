package me.crz.minecraft.kingdoms.village.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ModeGUI extends KingdomsGUI {
    private static ModeGUI instance;
    public CInventory gui;

    public String yesOrNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    private ModeGUI() {
        super("mode");
        gui = new CInventory(getText("title"), InventorySize.SIX) {};

        for (int i = 0; i < InventorySize.TWO.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        for (PlotMode m : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    if (village != null) {
                        boolean enabled = m.get(village);
                        String state = yesOrNo(enabled);
                        CItem item = m.getItem().setName(getMessage("entry.title").getReplacedText(
                                "mode", m.getTag(),
                                "value", state
                        )).setLore(getMessage("entry.lore").getReplacedTextList(
                                "mode", m.getTag(),
                                "value", state
                        ));
                        if (enabled) {
                            item.glow(true);
                        }
                        return item.build();
                    }
                    return null;
                }

                public boolean click(Player p, ClickType clickType) {
                    Resident res = ResidentManager.getInstance().getResident(p);
                    if (res.hasVillage() && res.hasPermission("village.mode")) {
                        boolean state = !m.get(res.getVillage());
                        if (m.set(state, res.getVillage())) {
                            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
                        }
                        MessageUtils.tell(p, Message.get("command.village.mode.success"),
                                "mode", m.getTag(),
                                "value", yesOrNo(state)
                        );
                        gui.build(p);
                        return true;
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

    synchronized public static ModeGUI getInstance() {
        if (instance == null) {
            instance = new ModeGUI();
        }
        return instance;
    }
}
