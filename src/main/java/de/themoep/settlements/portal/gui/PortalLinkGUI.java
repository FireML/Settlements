package de.themoep.settlements.portal.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
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
    private static final String[] SETUP = {
            "ttttttttt",
            "ttttttttt",
            "p   c   n",
    };
    private final InventoryGui gui;
    private final Resident resident;

    public PortalLinkGUI(Portal portal, Resident resident) {
        super("link");
        this.resident = resident;
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP,
                new GuiPageElement('p', new ItemStack(Material.ARROW), GuiPageElement.PageAction.PREVIOUS, getText("previous")),
                new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, getText("previous")),
                new StaticGuiElement('c', new ItemStack(Material.BARRIER), click -> {
                    click.getGui().close();
                    return true;
                }, getText("close"))
        );

        GuiElementGroup portalGroup = new GuiElementGroup('t');

        List<Portal> portals = new ArrayList<>();

        for (OwnedLand land : resident.getVillage().getLand()) {
            portals.addAll(PortalManager.getInstance().getPortals(land));
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

    public void show() {
        Player player = resident.getPlayer();
        if (gui != null && resident.hasVillage() && resident.getVillage().canAdminPortals(resident) && player != null) {
            gui.show(player);
        }
    }
}
