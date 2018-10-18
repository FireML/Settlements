package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.cinventory.CInventory;
import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.VillageGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class PlotBuildGui extends VillageGUI {
    private static PlotBuildGui instance;
    private CInventory gui;

    public PlotBuildGui() {
        super("plot-build");
        gui = new CInventory(getText("title"), InventorySize.ONE) {};
        for (PlotBuildState state : PlotBuildState.values()) {
            gui.addItem(new ClickableItem() {
                @Override
                public ItemStack getItem(Player p) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null) {
                        if (state == land.getBuildState()) {
                            return state.getItem()
                                    .setName(getMessage("selected-state").getReplacedText("state", state.getTag()))
                                    .setLore(getMessage("selected-state").getReplacedTextList("state", state.getTag()).stream().skip(1).collect(Collectors.toList()))
                                    .glow(true)
                                    .build();
                        }
                        return state.getItem()
                                .setName(getMessage("set-state").getReplacedText("state", state.getTag()))
                                .setLore(getMessage("set-state").getReplacedTextList("state", state.getTag()).stream().skip(1).collect(Collectors.toList()))
                                .build();
                    }
                    return null;
                }

                @Override
                public boolean click(Player p, ClickType clickType) {
                    OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land != null && state != land.getBuildState() && land.isPlotOwner(p)) {
                        land.setBuildState(state);
                        MessageUtils.tell(p, Message.get("command.village.plot.build.success"),
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

    public static PlotBuildGui getInstance() {
        if (instance == null) {
            instance = new PlotBuildGui();
        }
        return instance;
    }
}
