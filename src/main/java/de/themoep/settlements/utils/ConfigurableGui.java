package de.themoep.settlements.utils;

import de.themoep.settlements.SettlementsGUI;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.resident.Resident;
import org.bukkit.entity.Player;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiBackElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.GuiPageElement;
import uk.firedev.poleislib.libs.themoep.inventorygui.InventoryGui;

public class ConfigurableGui extends SettlementsGUI {
    protected final Resident resident;
    protected InventoryGui gui;

    public ConfigurableGui(Resident resident, String name, String... replacements) {
        super(name);
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title", replacements), getSetup());
        gui.setCloseAction(close -> false);
        gui.setFiller(getIcon("filler"));
        gui.addElement(new GuiBackElement('b', getIcon("back"), true, getText("back")));
        gui.addElement(new GuiPageElement('n', getIcon("next"), GuiPageElement.PageAction.NEXT, getText("next")));
        gui.addElement(new GuiPageElement('p', getIcon("previous"), GuiPageElement.PageAction.PREVIOUS, getText("previous")));

        gui.addElements(getConfiguredElements());
    }

    public void open() {
        if (gui != null && resident != null) {
            Player player = resident.getPlayer();
            if (player != null) {
                gui.show(resident.getPlayer());
            }
        }
    }
}
