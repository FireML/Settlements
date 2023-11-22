package de.themoep.settlements.utils;

import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.firedev.poleislib.Loggers;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.InventoryGui;
import uk.firedev.poleislib.libs.themoep.inventorygui.StaticGuiElement;

import java.util.function.Consumer;
import java.util.logging.Level;

public class ConfirmationGui extends SettlementsGUI {
    private final InventoryGui gui;

    public ConfirmationGui(String name, Consumer<GuiElement.Click> confirmedAction, Consumer<GuiElement.Click> cancelAction, String... replacements) {
        super(name);
        if (getSetup().length == 0) {
            Loggers.log(Level.SEVERE, SettlementsPlugin.getInstance().getLogger(), "Missing setup for GUI " + name + " in guis.yml!");
        }
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title", replacements), getSetup());
        gui.addElements(getConfiguredElements());

        gui.addElement(new StaticGuiElement('c', getIcon("confirm", new ItemStack(Material.GREEN_WOOL)), click -> {
            confirmedAction.accept(click);
            return true;
        }, getText("confirm", replacements)));

        gui.addElement(new StaticGuiElement('b', getIcon("cancel", new ItemStack(Material.RED_WOOL)), click -> {
            cancelAction.accept(click);
            return true;
        }, getText("cancel", replacements)));
    }

    public void open(Player player) {
        gui.show(player);
    }
}
