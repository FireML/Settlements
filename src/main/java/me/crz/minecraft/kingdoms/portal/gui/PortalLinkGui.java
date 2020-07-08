package me.crz.minecraft.kingdoms.portal.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.portal.Portal;
import me.crz.minecraft.kingdoms.portal.PortalManager;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PortalLinkGui implements ConfigMessages {
    private static final String[] SETUP = {
            "ttttttttt",
            "ttttttttt",
            "p   c   n",
    };
    private static final ItemStack PORTAL;
    static {
        PORTAL = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) PORTAL.getItemMeta();
        PlayerProfile portalProfile = Bukkit.createProfile(UUID.fromString("fd39933c-3068-4a5d-ac96-0d470ceff502"), "Nether Portal");
        portalProfile.setProperty(new ProfileProperty("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjZlNjY5NmI2MzczOGJiY2M1ZmQ5NzM3MDllYTkwOTk3MjE1YjBjZmVlY2UyMGIyNjU5Y2I3YjM1ZjFiZjAifX19"));
        meta.setPlayerProfile(portalProfile);
        PORTAL.setItemMeta(meta);
    }
    private final InventoryGui gui;
    private final Resident resident;

    public PortalLinkGui(Portal portal, Resident resident) {
        this.resident = resident;
        gui = new InventoryGui(KingdomsPlugin.getInstance(), getText("title"), SETUP,
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

    @Override
    public String type() {
        return "gui.portal";
    }

    @Override
    public String name() {
        return "link";
    }
}
