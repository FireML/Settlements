package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ModeGUI extends PlotGUI {
    private static ModeGUI instance;
    public CInventory gui;

    public String yesOrNo(final boolean b) {
        return b ? "Yes" : "No";
    }

    private ModeGUI() {
        super("plot.mode");
        gui = new CInventory(getText("title"), InventorySize.SIX) {};

        for (int i = 0; i < InventorySize.TWO.getSize(); i++) {
            gui.addItem(i, BORDER);
        }

        for (PlotMode m : PlotMode.values(PlotMode.AppliesTo.LAND)) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null) {
                        boolean enabled = m.get(land);
                        String state = yesOrNo(enabled);
                        CItem item = m.getItem().setName(getMessage("entry.title").getReplacedText(
                                "mode", m.getTag(),
                                "value", state
                        )).addLore(getMessage("entry.lore").getReplacedTextList(
                                "mode", m.getTag(),
                                "value", state
                        ));
                        if (m.appliesTo(PlotMode.AppliesTo.LAND_OWNER)) {
                            if (land.hasMode(m)) {
                                item.addLore(getMessage("entry.click-to-unset").getReplacedTextList(
                                        "parent", land.getLandOwner().getStateTag(),
                                        "value", yesOrNo(land.getLandOwner().getMode(m))
                                ));
                            } else {
                                item.addLore(getMessage("entry.parent").getReplacedTextList(
                                        "parent", land.getLandOwner().getStateTag()
                                ));
                            }
                        }
                        if (enabled) {
                            item.glow(true);
                        }
                        return item.build();
                    }
                    return null;
                }

                public boolean click(Player p, ClickType clickType) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null) {
                        Resident resident = ResidentManager.getInstance().getResident(p);
                        boolean canAdmin = land.canAdmin(resident);
                        if (land.isPlotOwner(p) || canAdmin) {
                            if (m == PlotMode.embassy) {
                                if (land.containsHome() || land.isOutpost()) {
                                    getError("command.plot.mode.cant-set-embassy").tell(p);
                                    return false;
                                } else if (!canAdmin) {
                                    return false;
                                }
                            }
                            if (clickType == ClickType.MIDDLE) {
                                if (land.unsetMode(m) || land.hasLink()) {
                                    Message.get("command.plot.mode.unset").tell(p,
                                            "mode", m.getTag(),
                                            "value", yesOrNo(land.getLandOwner().getMode(m)),
                                            "parent", land.getLandOwner().getStateTag()
                                    );
                                } else {
                                    return false;
                                }
                            } else {
                                boolean state = !m.get(land);
                                land.setMode(m, state);
                                Message.get("command.plot.mode.success").tell(p,
                                        "mode", m.getTag(),
                                        "value", yesOrNo(state)
                                );
                            }
                            LandManager.getInstance().syncAndSave(land);
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
                new SettingsGUI(p);
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
