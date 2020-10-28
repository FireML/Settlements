package me.crz.minecraft.kingdoms.portal.gui;

import me.crz.minecraft.kingdoms.KingdomsGUI;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public abstract class PortalGui extends KingdomsGUI {
    protected static final ItemStack PORTAL;
    static {
        PORTAL = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) PORTAL.getItemMeta();
        KingdomsPlugin.getInstance().getServerAdapter().setProfile(
                meta,
                UUID.fromString("fd39933c-3068-4a5d-ac96-0d470ceff502"),
                "Nether Portal",
                "textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjZlNjY5NmI2MzczOGJiY2M1ZmQ5NzM3MDllYTkwOTk3MjE1YjBjZmVlY2UyMGIyNjU5Y2I3YjM1ZjFiZjAifX19"
        );
        PORTAL.setItemMeta(meta);
    }

    public PortalGui(String name) {
        super("portal." + name);
    }
}
