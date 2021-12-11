package de.themoep.settlements.land.gui;

import de.themoep.inventorygui.InventoryGui;
import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.resident.Resident;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AllyGUI extends SettlementsGUI {
    private final Resident resident;
    private final InventoryGui gui;

    public AllyGUI(@NonNull String type, Resident resident, Ally ally) {
        super(type + ".allies");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElements(getConfiguredElements());

        // TODO: Ally options
    }

    public void open() {
        gui.show(resident.getPlayer());
    }
}
