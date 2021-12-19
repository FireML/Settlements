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
    private final InventoryGui gui;

    public ConfirmationGui(String name, Consumer<GuiElement.Click> confirmedAction, Consumer<GuiElement.Click> cancelAction, String... replacements) {
        super(name);
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText(name + ".title", replacements), getSetup());
        gui.addElements(getConfiguredElements());

        gui.addElement(new StaticGuiElement('c', new ItemStack(Material.GREEN_WOOL), click -> {
            confirmedAction.accept(click);
            return true;
        }, getText("confirm", replacements)));

        gui.addElement(new StaticGuiElement('b', new ItemStack(Material.RED_WOOL), click -> {
            cancelAction.accept(click);
            return true;
        }, getText("cancel", replacements)));
    }

    public void open(Player player) {
        gui.show(player);
    }
}
