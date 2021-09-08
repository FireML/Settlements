package de.themoep.settlements.utils;

import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfirmationGui {
    private final static String[] SETUP = {
            "   c  b   "
    };
    private final InventoryGui gui;

    public ConfirmationGui(ConfigMessages configMessages, String name, Runnable confirmedAction, Runnable cancelAction, String... replacements) {
        gui = new InventoryGui(SettlementsPlugin.getInstance(), configMessages.getText(name + ".title", replacements), SETUP);

        gui.addElement(new StaticGuiElement('c', new ItemStack(Material.GREEN_WOOL), click -> {
            confirmedAction.run();
            return true;
        }, configMessages.getText(name + ".confirm", replacements)));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.RED_WOOL), click -> {
            cancelAction.run();
            return true;
        }, configMessages.getText(name + ".cancel", replacements)));
    }

    public void open(Player player) {
        gui.show(player);
    }
}
