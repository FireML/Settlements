package de.themoep.settlements.customevents.listeners;

import de.themoep.settlements.customevents.CreaturePreSpawnEvent;
import de.themoep.settlements.customevents.EntityPrimeTntEvent;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.village.Settlement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Iterator;

public class SpigotListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        // Call hostile pre creature spawn event as normal spawn event to fall back on it :S
        CreaturePreSpawnEvent hcse = new CreaturePreSpawnEvent(e.getEntity().getType(), e.getLocation(), e.getSpawnReason());
        Bukkit.getPluginManager().callEvent(hcse);
        if (hcse.isCancelled()) {
            e.setCancelled(true);
        }
    }

    // Try to simulate TNT prime event
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowTntHit(ProjectileHitEvent e) {
        if (e.getHitBlock() != null && e.getHitBlock().getType() == Material.TNT && !e.getEntity().doesBounce()) {
            EntityPrimeTntEvent primeEvent = null;
            if (e.getEntityType() == EntityType.WITHER_SKULL || e.getEntity() instanceof Fireball) {
                primeEvent = new EntityPrimeTntEvent(e.getHitBlock(), EntityPrimeTntEvent.Reason.PROJECTILE, e.getEntity());
            } else if (e.getEntity() instanceof AbstractArrow && e.getEntity().getFireTicks() > 0) {
                primeEvent = new EntityPrimeTntEvent(e.getHitBlock(), EntityPrimeTntEvent.Reason.PROJECTILE, e.getEntity());
            }
            if (primeEvent != null) {
                Bukkit.getPluginManager().callEvent(primeEvent);
                if (primeEvent.isCancelled()) {
                    e.getEntity().setBounce(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTntBurn(BlockBurnEvent e) {
        if (e.getBlock().getType() == Material.TNT) {
            EntityPrimeTntEvent primeEvent = new EntityPrimeTntEvent(e.getBlock(), EntityPrimeTntEvent.Reason.FIRE, null);
            if (primeEvent.isCancelled()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTntInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.TNT
                && e.getItem() != null && (e.getItem().getType() == Material.FLINT_AND_STEEL || e.getItem().getType() == Material.FIRE_CHARGE)) {
            EntityPrimeTntEvent primeEvent = new EntityPrimeTntEvent(e.getClickedBlock(), EntityPrimeTntEvent.Reason.ITEM, e.getPlayer());
            if (primeEvent.isCancelled()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRedstoneTnt(BlockRedstoneEvent e) {
        if (e.getBlock().getType() == Material.TNT) {
            EntityPrimeTntEvent primeEvent = new EntityPrimeTntEvent(e.getBlock(), EntityPrimeTntEvent.Reason.REDSTONE, null);
            if (primeEvent.isCancelled()) {
                e.setNewCurrent(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionTnt(BlockExplodeEvent e) {
        for (Iterator<Block> it = e.blockList().iterator(); it.hasNext(); ) {
            Block b = it.next();
            if (b.getType() == Material.TNT) {
                EntityPrimeTntEvent primeEvent = new EntityPrimeTntEvent(b, EntityPrimeTntEvent.Reason.EXPLOSION, null);
                if (primeEvent.isCancelled()) {
                    it.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionTnt(EntityExplodeEvent e) {
        for (Iterator<Block> it = e.blockList().iterator(); it.hasNext(); ) {
            Block b = it.next();
            if (b.getType() == Material.TNT) {
                EntityPrimeTntEvent primeEvent = new EntityPrimeTntEvent(b, EntityPrimeTntEvent.Reason.EXPLOSION, e.getEntity());
                if (primeEvent.isCancelled()) {
                    it.remove();
                }
            }
        }
    }

    // Try to catch portal removal
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPortalDestroy(BlockPhysicsEvent e) {
        if (!Config.Portal.ENABLED || e.getBlock().getType() != Material.NETHER_PORTAL || e.getBlock().getType() == e.getChangedType()) {
            return;
        }

        OwnedLand land = LandManager.getInstance().getLand(e.getBlock().getLocation());
        if (land == null || !(land.getLandOwner() instanceof Settlement)) {
            return;
        }

        Portal portal = PortalManager.getInstance().getPortal(e.getBlock());

        if (portal != null && PortalManager.getInstance().removePortal(portal)) {
            Message.get("portal.destroy-broadcast").tell(land.getLandOwner().getOnlineResidents(), portal.getReplacements());
        }
    }
}
