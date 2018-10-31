package me.crz.minecraft.kingdoms.land.gui;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.landflags.PlotBuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UseGui extends KingdomsGUI {
    private static UseGui instance;
    private CInventory gui;

    public UseGui() {
        super("plot.use");
        gui = new CInventory(getText("title"), InventorySize.FOUR) {};
        for (Use u : Use.values()) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null) {
                        PlotBuildState state = land.getUseState(u);
                        return u.getItem().setName(getMessage("entry").getReplacedText(
                                "flag", u.getTag(),
                                "state", state.getTag()
                        )).build();
                    }
                    return null;
                }

                public boolean click(Player p, ClickType clickType) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null && land.isPlotOwner(p)) {
                        PlotBuildState next = PlotBuildState.next(land.getUseState(u));
                        land.setUseState(u, next);
                        MessageUtils.tell(p, Message.get("command.village.plot.use.success"),
                                "flag", u.getTag(),
                                "state", next.getTag()
                        );
                        LandManager.getInstance().syncAndSave(land);
                        gui.build(p);
                        return true;
                    }
                    return false;
                }
            });
        }

        int rest = gui.getSize() % 9;
        int slot = rest == 0 ? gui.getSize() : gui.getSize() + (9 - rest);
        for (PlotBuildState state : PlotBuildState.values()) {
            gui.addItem(slot++, new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null && land.isPlotOwner(p)) {
                        return state.getItem()
                                .setName(getMessage("toggle-all").getReplacedText("state", state.getTag()))
                                .setLore(getMessage("toggle-all").getReplacedTextList("state", state.getTag()).stream().skip(1).collect(Collectors.toList()))
                                .build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null && land.isPlotOwner(p)) {
                        for (Use u : Use.values()) {
                            land.setUseState(u, state);
                        }
                        MessageUtils.tell(p, Message.get("command.village.plot.use.success-all"),
                                "state", state.getTag()
                        );
                        LandManager.getInstance().syncAndSave(land);
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
                SettingsGUI.getInstance().open(p);
                return true;
            }
        });
    }

    public void open(final Player p) {
        gui.build(p);
    }

    public static UseGui getInstance() {
        if (instance == null) {
            instance = new UseGui();
        }
        return instance;
    }
}
