package me.crz.minecraft.kingdoms.landflags;

import me.crz.minecraft.kingdoms.cinventory.CInventoryManager;
import me.crz.minecraft.kingdoms.village.BuildState;
import me.crz.minecraft.kingdoms.village.Village;

import java.util.Arrays;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.command.village.settings.SettingsGUI;
import org.bukkit.event.inventory.ClickType;
import me.crz.minecraft.kingdoms.cinventory.CItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.cinventory.ClickableItem;
import me.crz.minecraft.kingdoms.cinventory.InventorySize;
import me.crz.minecraft.kingdoms.cinventory.CInventory;

public class UseGui {
    private static UseGui instance;
    public CInventory gui;

    public static String cfc(final String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        return String.valueOf(string.substring(0, 1).toUpperCase()) + string.substring(1, string.length());
    }

    public UseGui() {
        super();
        gui = new CInventory("&3&lUse Settings", InventorySize.FOUR) {};
        Integer[] array;
        for (int n = (array = new Integer[]{27, 28, 34, 35}).length, j = 0; j < n; ++j) {
            final int i = array[j];
            gui.addItem(i, new ClickableItem() {
                public ItemStack getItem(final Player p) {
                    if (i != 35) {
                        return new CItem(Material.BLACK_STAINED_GLASS_PANE).build();
                    }
                    return new CItem(Material.BARRIER).setName("&c&lBack To Settings").build();
                }

                public boolean click(final Player p, final ClickType clickType) {
                    if (i == 35) {
                        SettingsGUI.getInstance().open(p);
                        return true;
                    }
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
                        final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                        final CItem item = u.getItem();
                        item.setLore(Arrays.asList(MessageUtils.replaceColorCodes("&7&lState"), MessageUtils.replaceColorCodes("&b&l" + UseGui.cfc(village.getUseState(u).name()))));
                        return item.build();
                    }
                    return null;
                }

                public boolean click(final Player p, final ClickType clickType) {
                    if (!ResidentManager.getInstance().doesResidentHaveVillage(p)) {
                        return false;
                    }
                    final Village village = VillageManager.getInstance().getVillageFromPlayer(p);
                    if (village.getChief().equals(p.getUniqueId())) {
                        village.setUseState(u, BuildState.next(village.getUseState(u)));
                        MessageUtils.tell(p, "&7Use state &6" + u.name().toLowerCase() + "&7 changed to &6" + village.getUseState(u));
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

    public static UseGui getInstance() {
        if (instance == null) {
            instance = new UseGui();
        }
        return instance;
    }
}
