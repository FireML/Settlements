package de.themoep.settlements.portal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.portal.gui.PortalTeleportGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
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
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PortalListener implements Listener, ConfigMessages {

    private final Cache<UUID, Long> lastPortalInteract = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

    @EventHandler(ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent e) {
        if (e.getReason() == PortalCreateEvent.CreateReason.END_PLATFORM) {
            return;
        }

        if (!Utils.isWorldClaimable(e.getWorld())) {
            if (Config.Portal.IGNORE_UNCLAIMABLE_WORLDS) {
                return;
            } else if (!Config.Portal.ONLY_INSIDE_CLAIMS) {
                return;
            }
        }

        if (!Config.Portal.ALLOW_NETHER_TELEPORT && e.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR) {
            e.setCancelled(true);
            return;
        }

        Entity entity = e.getEntity();
        if (entity == null && e.getReason() == PortalCreateEvent.CreateReason.FIRE) {
            Location l = e.getBlocks().get(0).getLocation();
            double distance = Double.MAX_VALUE;
            for (Entity nearbyPlayer : l.getWorld().getNearbyEntities(l, 5, 5, 5, en -> en instanceof Player)) {
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
                if (land == null) {
                    if (Config.Portal.ONLY_INSIDE_CLAIMS) {
                        e.setCancelled(true);
                        getMessage("only-in-claim").tell(entity);
                    }
                    return;
                } else if (land.isEmbassy() && !land.getLandOwner().isMember(resident)
                        && (land.isPlotOwner(resident) || land.getPlotOwnerResident().isFriend(resident))) {
                    if (!resident.hasSettlement() || !resident.getSettlement().canAdminPortals(resident)) {
                        e.setCancelled(true);
                        getMessage("not-allowed.create").tell(e.getEntity(), "state", land.getLandOwner().getStateTag());
                        return;
                    }
                } else if (!land.getLandOwner().canAdminPortals(resident)) {
                    e.setCancelled(true);
                    getMessage("not-allowed.create").tell(e.getEntity(), "state", land.getLandOwner().getStateTag());
                    return;
                }
            }

            if (Utils.isWorldClaimable(e.getWorld())) {
                if ((resident.hasSettlement()
                        && resident.getSettlement().canAdminPortals(resident)
                        && resident.getSettlement().canClaim(e.getWorld())
                ) || (resident.hasEmpire()
                        && resident.getSettlement().getEmpire().canAdminPortals(resident)
                        && resident.getSettlement().getEmpire().canClaim(e.getWorld()))) {
                    PortalManager.getInstance().addPortal(e.getBlocks(), entity);
                    getMessage("created").tell(entity);
                }
            }
        } else {
            // Not possible to support non-player portal creation
            e.setCancelled(true);
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
        if (land == null || !(land.getLandOwner() instanceof Settlement)) {
            return;
        }

        Long lastInteract = lastPortalInteract.getIfPresent(e.getEntity().getUniqueId());
        if (lastInteract != null && lastInteract + 1000 > System.currentTimeMillis()) {
            return;
        }
        lastPortalInteract.put(e.getEntity().getUniqueId(), System.currentTimeMillis());

        Portal portal = PortalManager.getInstance().getPortal(block);
        if (portal == null) {
            getError("not-registered").tell(e.getEntity());
            return;
        }

        if (portal.getLinks().isEmpty()) {
            Resident resident = ResidentManager.getInstance().getResident((Player) e.getEntity());
            if (land.getLandOwner().canAdminPortals(resident)) {
                getError("no-portals-linked.create").tell(resident, "state", land.getLandOwner().getStateTag());
            } else {
                getError("no-portals-linked.common").tell(resident, "state", land.getLandOwner().getStateTag());
            }
        } else {
            Collection<Portal> links = PortalManager.getInstance().getLinks(portal);
            if (links.size() == 1) {
                PortalManager.getInstance().teleport((Player) e.getEntity(), portal, links.iterator().next());
            } else if (!links.isEmpty()) {
                e.getEntity().teleport(e.getLocation().subtract(e.getLocation().getDirection().setY(0)));
                Utils.runLater(() -> new PortalTeleportGUI(ResidentManager.getInstance().getResident((Player) e.getEntity()), portal, links).open(), 2);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalTeleport(EntityPortalEvent e) {
        if (!Config.Portal.ALLOW_NETHER_TELEPORT) {
            // Disable normal portal mechanic
            e.setCancelled(true);
        } else {
            Portal portal = PortalManager.getInstance().getPortal(e.getFrom());
            // Check if a portal exists and if so if it has links. If it does, cancel teleport
            if (portal != null && !portal.getLinks().isEmpty()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalTeleport(PlayerPortalEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (!Config.Portal.ALLOW_NETHER_TELEPORT) {
                // Disable normal portal mechanic
                e.setCancelled(true);
            } else {
                Portal portal = PortalManager.getInstance().getPortal(e.getFrom());
                // Check if a portal exists and if so if it has links. If it does, cancel teleport
                if (portal != null && !portal.getLinks().isEmpty()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.NETHER_PORTAL && e.getBlock().getType() != Material.OBSIDIAN) {
            return;
        }

        OwnedLand land = LandManager.getInstance().getLand(e.getBlock().getLocation());
        if (land == null) {
            return;
        }

        Portal portal = PortalManager.getInstance().getPortal(e.getBlock());

        if (portal != null) {
            Resident resident = ResidentManager.getInstance().getResident(e.getPlayer());
            if (!land.getLandOwner().canAdminPortals(resident) && !land.isEmbassy() && !land.isOwnedBy(resident)
                    && (!resident.hasSettlement() || !resident.getSettlement().canAdminPlots(resident))) {
                e.setCancelled(true);
                getMessage("not-allowed.destroy").tell(resident, "state", land.getLandOwner().getStateTag());
            } else if (PortalManager.getInstance().removePortal(portal)) {
                getMessage("destroyed").tell(resident, portal.getReplacements());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPortalExplode(BlockExplodeEvent e) {
        handleExplosion(e.blockList());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPortalExplode(EntityExplodeEvent e) {
        handleExplosion(e.blockList());
    }

    private void handleExplosion(List<Block> blockList) {
        for (Block block : blockList) {
            if (block.getType() != Material.NETHER_PORTAL && block.getType() != Material.OBSIDIAN) {
                continue;
            }

            OwnedLand land = LandManager.getInstance().getLand(block.getLocation());
            if (land == null || !(land.getLandOwner() instanceof Settlement)) {
                continue;
            }

            Portal portal = PortalManager.getInstance().getPortal(block);

            if (portal != null && PortalManager.getInstance().removePortal(portal)) {
                getMessage("destroy-broadcast").tell(land.getLandOwner().getOnlineResidents(), portal.getReplacements());
            }
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
