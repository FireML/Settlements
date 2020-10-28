package me.crz.minecraft.kingdoms.portal.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.portal.Portal;
import me.crz.minecraft.kingdoms.portal.PortalManager;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class PortalUnlinkGui extends PortalGui {
    private static final String[] SETUP = {
            "ttttttttt",
            "ttttttttt",
            "p   c   n",
    };
    private final InventoryGui gui;

    public PortalUnlinkGui(Portal portal) {
        super("unlink");
        Collection<Portal> links = PortalManager.getInstance().getLinks(portal);
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP,
                new GuiPageElement('p', new ItemStack(Material.ARROW), GuiPageElement.PageAction.PREVIOUS, getText("previous")),
                new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, getText("previous")),
                new StaticGuiElement('c', new ItemStack(Material.BARRIER), click -> {
                    click.getGui().close();
                    return true;
                }, getText("close"))
        );

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

    public void show(Resident resident) {
        Player player = resident.getPlayer();
        if (gui != null && resident.hasVillage() && resident.getVillage().canAdminPortals(resident) && player != null) {
            gui.show(player);
        }
    }
}
