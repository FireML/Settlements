package de.themoep.settlements.portal.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.resident.Resident;

import java.util.ArrayList;
import java.util.List;

public class PortalLinkGUI extends PortalGUI {

    public PortalLinkGUI(Portal portal, Resident resident) {
        super(resident, "link");

        GuiElementGroup portalGroup = new GuiElementGroup('t');

        List<Portal> portals = new ArrayList<>();

        for (OwnedLand land : resident.getSettlement().getLand()) {
            if (!land.isEmbassy()) {
                portals.addAll(PortalManager.getInstance().getPortals(land));
            }
        }

        if (resident.hasEmpire()) {
            for (OwnedLand land : resident.getSettlement().getEmpire().getLand()) {
                if (!land.isEmbassy()) {
                    portals.addAll(PortalManager.getInstance().getPortals(land));
                }
            }
        }

        for (OwnedLand land : LandManager.getInstance().getOwnedPlot(resident.getUUID())) {
            if (!land.isOwnedBy(resident) && land.isEmbassy()) {
                portals.addAll(PortalManager.getInstance().getPortals(land));
            }
        }

        for (Portal available : portals) {
            if (available == portal || portal.getLinks().contains(available.getId())) {
                continue;
            }
            portalGroup.addElement(new StaticGuiElement('t', PORTAL, click -> {
                if (PortalManager.getInstance().linkPortals(portal, available)) {
                    getMessage("success").tell(click.getWhoClicked(), available.getReplacements());
                } else {
                    getMessage("error").tell(click.getWhoClicked(), available.getReplacements());
                }
                click.getGui().close();
                return true;
            }, getText("portal", available.getReplacements())));
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
