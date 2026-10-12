package de.themoep.settlements.portal.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PortalTeleportGUI extends PortalGUI {

    public PortalTeleportGUI(Resident resident, Portal portal, Collection<Portal> links) {
        super(resident, "teleport");

        GuiElementGroup teleports = new GuiElementGroup('t');

        for (Portal linked : links) {
            teleports.addElement(new StaticGuiElement('t', PORTAL, click -> {
                click.getGui().close();
                Utils.runLater(() -> PortalManager.getInstance().teleport((Player) click.getWhoClicked(), portal, linked), 2);
                return true;
            }, getText("portal", linked.getReplacements())));
        }

        gui.addElement(teleports);
    }
}
