package de.themoep.settlements.utils;

import de.themoep.inventorygui.GuiBackElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.resident.Resident;

public class ConfigurableGui extends SettlementsGUI {
    protected final Resident resident;
    protected final InventoryGui gui;

    public ConfigurableGui(Resident resident, String name, String... replacements) {
        super(name);
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title", replacements), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());

        gui.addElement(new GuiBackElement('b', getIcon("back"), getText("back")));
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
