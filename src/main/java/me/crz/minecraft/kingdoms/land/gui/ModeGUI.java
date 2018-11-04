package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
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
        gui = new CInventory(getText("title"), InventorySize.ONE) {};

        for (PlotMode m : PlotMode.values()) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null) {
                        boolean enabled = m.get(land);
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
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null) {
                        Resident resident = ResidentManager.getInstance().getResident(p);
                        boolean canAdmin = resident.hasVillage() && land.isOwnedBy(resident.getVillage()) && resident.getVillageRank().atLeast(VillageRank.ELDER);
                        if (land.isPlotOwner(p) || canAdmin) {
                            if (m == PlotMode.embassy) {
                                if (land.containsHome() || land.isOutpost()) {
                                    getError("command.village.plot.mode.cant-set-embassy").tell(p);
                                    return false;
                                } else if (!canAdmin) {
                                    return false;
                                }
                            }
                            boolean state = !m.get(land);
                            m.set(state, land);
                            LandManager.getInstance().syncAndSave(land);
                            MessageUtils.tell(p, Message.get("command.village.plot.mode.success"),
                                    "mode", m.getTag(),
                                    "value", yesOrNo(state)
                            );
                            gui.build(p);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        gui.addItem((gui.getMaxSlot() / 9) * 9 + 7, getPlotLinkItem(gui));
        gui.addItem((gui.getMaxSlot() / 9) * 9 + 8, new ClickableItem() {
            public ItemStack getItem(final Player p) {
                return new CItem(Material.BARRIER).setName(getText("back")).build();
            }

            public boolean click(final Player p, final ClickType clickType) {
                SettingsGUI.getInstance().open(p);
                return true;
            }
        });
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static ModeGUI getInstance() {
        if (instance == null) {
            instance = new ModeGUI();
        }
        return instance;
    }
}
