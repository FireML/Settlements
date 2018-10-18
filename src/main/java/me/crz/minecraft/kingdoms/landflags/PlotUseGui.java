package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class PlotUseGui extends VillageGUI {
    private static PlotUseGui instance;
    private CInventory gui;

    public PlotUseGui() {
        super("plot-use");
        gui = new CInventory(getText("title"), InventorySize.FOUR) {};
        for (Use u : Use.values()) {
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(Player p) {
                    if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                        OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                        PlotBuildState state = land.getUseState(u);
                        return u.getItem().setName(getMessage("entry.title").getReplacedText(
                                "flag", u.getTag(),
                                "state", state.getTag()
                        )).setLore(getMessage("entry.lore").getReplacedTextList(
                                "flag", u.getTag(),
                                "state", state.getTag()
                        )).build();
                    }
                    return null;
                }

                public boolean click(Player p, ClickType clickType) {
                    if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                        return false;
                    }
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land.isPlotOwner(p)) {
                        PlotBuildState next = PlotBuildState.next(land.getUseState(u));
                        land.setUseState(u, next);
                        MessageUtils.tell(p, Message.get("command.village.plot.use.success"),
                                "flag", u.getTag(),
                                "state", next.getTag()
                        );
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
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
                    if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                        return state.getItem()
                                .setName(getMessage("toggle-all").getReplacedText("state", state.getTag()))
                                .setLore(getMessage("toggle-all").getReplacedTextList("state", state.getTag()).stream().skip(1).collect(Collectors.toList()))
                                .build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                        return false;
                    }
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land.isPlotOwner(p)) {
                        for (Use u : Use.values()) {
                            land.setUseState(u, state);
                        }
                        MessageUtils.tell(p, Message.get("command.village.plot.use.success-all"),
                                "state", state.getTag()
                        );
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
                        gui.build(p);
                        return true;
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

    public static PlotUseGui getInstance() {
        if (instance == null) {
            instance = new PlotUseGui();
        }
        return instance;
    }
}
