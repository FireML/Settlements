package de.themoep.settlements.portal.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.resident.Resident;

import java.util.Collection;

public class PortalUnlinkGUI extends PortalGUI {

    public PortalUnlinkGUI(Resident resident, Portal portal) {
        super(resident, "unlink");
        Collection<Portal> links = PortalManager.getInstance().getLinks(portal);

        GuiElementGroup portalGroup = new GuiElementGroup('t');

        for (Portal linked : links) {
            portalGroup.addElement(new StaticGuiElement('t', PORTAL, click -> {
                if (PortalManager.getInstance().unlinkPortals(portal, linked)) {
                    getMessage("success").tell(click.getEvent().getWhoClicked(), linked.getReplacements());
                } else {
                    getMessage("error").tell(click.getEvent().getWhoClicked(), linked.getReplacements());
                }
                click.getGui().close();
                return true;
            }, getText("unlink-portal", linked.getReplacements())));
        }

        gui.addElement(portalGroup);
    }

    @Override
    public void open() {
        if (resident.hasSettlement() && resident.getSettlement().canAdminPortals(resident)) {
            super.open();
        }
    }
}
