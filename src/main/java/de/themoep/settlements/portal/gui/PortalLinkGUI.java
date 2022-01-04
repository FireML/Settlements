package de.themoep.settlements.portal.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
                    getMessage("success").tell(click.getEvent().getWhoClicked(), available.getReplacements());
                } else {
                    getMessage("error").tell(click.getEvent().getWhoClicked(), available.getReplacements());
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
