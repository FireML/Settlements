package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;

import java.util.Arrays;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.cinventory.CInventory;

public class PlotUseGui {
    private static PlotUseGui instance;
    public CInventory gui;

    public static String cfc(final String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        return String.valueOf(string.substring(0, 1).toUpperCase()) + string.substring(1, string.length());
    }

    public PlotUseGui() {
        super();
        gui = new CInventory("&3&lPlot Use Settings", InventorySize.FOUR) {};
        Integer[] array;
        for (int n = (array = new Integer[]{27, 28, 34, 35}).length, j = 0; j < n; ++j) {
            final int i = array[j];
            gui.addItem(i, new ClickableItem() {

                public ItemStack getItem(final Player p) {
                    return new CItem(Material.BLACK_STAINED_GLASS_PANE).build();
                }

                public boolean click(final Player p, final ClickType clickType) {
                    return false;
                }
            });
        }
        Use[] values;
        for (int n = (values = Use.values()).length, j = 0; j < n; ++j) {
            final Use u = values[j];
            gui.addItem(new ClickableItem() {
                public ItemStack getItem(final Player p) {
                    if (ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                        final OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                        final CItem item = u.getItem();
                        item.setLore(Arrays.asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + PlotUseGui.cfc(land.getUseState(u).name()))));
                        return item.build();
                    }
                    return null;
                }

                public boolean click(final Player p, final ClickType clickType) {
                    if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                        return false;
                    }
                    final OwnedLand land = LandManager.getInstance().getLand(p.getLocation());
                    if (land.isPlotOwner(p)) {
                        land.setUseState(u, PlotBuildState.next(land.getUseState(u)));
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
                        MessageUtils.tell(p, "&7Use state &6" + u.name().toLowerCase() + "&7 changed to &6" + land.getUseState(u));
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
