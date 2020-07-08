package me.crz.minecraft.kingdoms.portal;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.portal.gui.PortalTeleportGui;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.Collection;

public class PortalListener implements Listener, ConfigMessages {

    private long lastMessage = 0;

    @EventHandler(ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent e) {
        if (e.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR) {
            // Disable normal portal mechanic
            e.setCancelled(true);
        } else if (e.getReason() == PortalCreateEvent.CreateReason.FIRE) {
            Entity entity = e.getEntity();
            if (entity == null) {
                Location l = e.getBlocks().get(0).getLocation();
                double distance = Double.MAX_VALUE;
                for (Player nearbyPlayer : l.getNearbyPlayers(5)) {
                    double d = nearbyPlayer.getLocation().distanceSquared(l);
                    if (d < distance || entity == null) {
                        distance = d;
                        entity = nearbyPlayer;
                    }
                }
            }
            if (entity instanceof Player) {
                Resident resident = ResidentManager.getInstance().getResident((Player) entity);
                for (BlockState block : e.getBlocks()) {
                    OwnedLand land = LandManager.getInstance().getLand(block.getLocation());
                    if (land == null || !(land.getLandOwner() instanceof Village)) {
                        e.setCancelled(true);
                        getMessage("only-in-claim").tell(entity);
                        return;
                    } else if (!land.getLandOwner().canAdminPortals(resident)) {
                        e.setCancelled(true);
                        getMessage("not-allowed.create").tell(e.getEntity(), "state", land.getLandOwner().getStateTag());
                        return;
                    }
                }

                PortalManager.getInstance().addPortal(e.getBlocks(), entity);
                getMessage("created").tell(entity);
            } else {
                // Not possible to support non-player portal creation
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPortalEnter(EntityPortalEnterEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Block block = e.getLocation().getBlock();
        if (block.getType() != Material.NETHER_PORTAL) {
            return;
        }

        OwnedLand land = LandManager.getInstance().getLand(e.getLocation());
        if (land == null || !(land.getLandOwner() instanceof Village)) {
            return;
        }

        Portal portal = PortalManager.getInstance().getPortal(block);
        if (portal == null) {
            if (lastMessage + 1000 < System.currentTimeMillis()) {
                getError("not-registered").tell(e.getEntity());
                lastMessage = System.currentTimeMillis();
            }
            return;
        }

        if (portal.getLinks().isEmpty()) {
            if (lastMessage + 1000 < System.currentTimeMillis()) {
                Resident resident = ResidentManager.getInstance().getResident((Player) e.getEntity());
                if (land.getLandOwner().canAdminPortals(resident)) {
                    getError("no-portals-linked.create").tell(resident, "state", land.getLandOwner().getStateTag());
                } else {
                    getError("no-portals-linked.common").tell(resident, "state", land.getLandOwner().getStateTag());
                }
                lastMessage = System.currentTimeMillis();
            }
        } else {
            Collection<Portal> links = PortalManager.getInstance().getLinks(portal);
            if (links.size() == 1) {
                PortalManager.getInstance().teleport((Player) e.getEntity(), portal, links.iterator().next());
            } else if (!links.isEmpty()) {
                e.getEntity().teleport(e.getLocation().subtract(e.getLocation().getDirection()));
                Utils.runLater(() -> new PortalTeleportGui(portal, links).show((Player) e.getEntity()), 2);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalTeleport(EntityPortalEvent e) {
        // Disable normal portal mechanic
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalTeleport(PlayerPortalEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            // Disable normal portal mechanic
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.NETHER_PORTAL && e.getBlock().getType() != Material.OBSIDIAN) {
            return;
        }

        OwnedLand land = LandManager.getInstance().getLand(e.getBlock().getLocation());
        if (land == null || !(land.getLandOwner() instanceof Village)) {
            return;
        }

        Portal portal = PortalManager.getInstance().getPortal(e.getBlock());

        if (portal != null) {
            Resident resident = ResidentManager.getInstance().getResident(e.getPlayer());
            if (!land.getLandOwner().canAdminPortals(resident)) {
                e.setCancelled(true);
                getMessage("not-allowed.destroy").tell(resident, "state", land.getLandOwner().getStateTag());
            } else if (PortalManager.getInstance().removePortal(portal)) {
                getMessage("destroyed").tell(resident, portal.getReplacements());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPortalDestroy(BlockDestroyEvent e) {
        if (e.getBlock().getType() != Material.NETHER_PORTAL && e.getBlock().getType() != Material.OBSIDIAN) {
            return;
        }

        OwnedLand land = LandManager.getInstance().getLand(e.getBlock().getLocation());
        if (land == null || !(land.getLandOwner() instanceof Village)) {
            return;
        }

        Portal portal = PortalManager.getInstance().getPortal(e.getBlock());

        if (portal != null && PortalManager.getInstance().removePortal(portal)) {
            getMessage("destroy-broadcast").tell(land.getLandOwner().getOnlineResidents(), portal.getReplacements());
        }
    }

    @Override
    public String type() {
        return "";
    }

    @Override
    public String name() {
        return "portal";
    }
}
