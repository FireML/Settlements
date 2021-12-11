package de.themoep.settlements.utils;

import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ConfirmationGui extends SettlementsGUI {
    private final static String[] SETUP = {
            "   c  b   "
    };
    private final InventoryGui gui;

    public ConfirmationGui(ConfigMessages configMessages, String name, Consumer<GuiElement.Click> confirmedAction, Consumer<GuiElement.Click> cancelAction, String... replacements) {
        super("confirmation." + name);
        gui = new InventoryGui(SettlementsPlugin.getInstance(), configMessages.getText(name + ".title", replacements), getSetup());
        gui.addElements(getConfiguredElements());

        gui.addElement(new StaticGuiElement('c', new ItemStack(Material.GREEN_WOOL), click -> {
            confirmedAction.accept(click);
            return true;
        }, configMessages.getText(name + ".confirm", replacements)));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.RED_WOOL), click -> {
            cancelAction.accept(click);
            return true;
        }, configMessages.getText(name + ".cancel", replacements)));
    }

    public void open(Player player) {
        gui.show(player);
    }
}
