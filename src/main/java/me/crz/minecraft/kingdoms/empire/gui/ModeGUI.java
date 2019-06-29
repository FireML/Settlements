package me.crz.minecraft.kingdoms.empire.gui;

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
        super("empire.mode");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        for (PlotMode m : PlotMode.values(PlotMode.AppliesTo.LAND_OWNER)) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    if (village != null && village.hasEmpire()) {
                        boolean enabled = m.get(village.getEmpire());
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
                    if (res.hasEmpire() && res.hasPermission("empire.mode")) {
                        boolean state = !m.get(res.getVillage().getEmpire());
                        if (m.set(state, res.getVillage().getEmpire())) {
                            Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(res.getVillage().getEmpire()));
                        }
                        MessageUtils.tell(p, Message.get("command.empire.mode.success"),
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

    synchronized public static ModeGUI getInstance() {
        if (instance == null) {
            instance = new ModeGUI();
        }
        return instance;
    }
}
