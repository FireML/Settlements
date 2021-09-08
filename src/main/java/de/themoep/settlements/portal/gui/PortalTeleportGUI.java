package de.themoep.settlements.portal.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class PortalTeleportGUI extends PortalGUI {
    private static final String[] SETUP = {
            "ttttttttt",
            "ttttttttt",
            "p   c   n",
    };
    private final InventoryGui gui;

    public PortalTeleportGUI(Portal portal, Collection<Portal> links) {
        super("teleport");
        gui = new InventoryGui(SettlementsPlugin.getInstance(), getText("title"), SETUP,
                new GuiPageElement('p', new ItemStack(Material.ARROW), GuiPageElement.PageAction.PREVIOUS, getText("previous")),
                new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, getText("previous")),
                new StaticGuiElement('c', new ItemStack(Material.BARRIER), click -> {
                    click.getGui().close();
                    return true;
                }, getText("close"))
        );

        GuiElementGroup teleports = new GuiElementGroup('t');

        for (Portal linked : links) {
            teleports.addElement(new StaticGuiElement('t', PORTAL, click -> {
                click.getGui().close();
                Utils.runLater(() -> PortalManager.getInstance().teleport((Player) click.getEvent().getWhoClicked(), portal, linked), 2);
                return true;
            }, getText("portal", linked.getReplacements())));
        }

        gui.addElement(teleports);
    }

    public void show(Player player) {
        gui.show(player);
    }
}
