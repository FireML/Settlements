package de.themoep.settlements.customevents.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import de.themoep.settlements.customevents.CreaturePreSpawnEvent;
import de.themoep.settlements.customevents.EntityPrimeTntEvent;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPhantomPreSpawn(PhantomPreSpawnEvent e) {
        if (!new CreaturePreSpawnEvent(e.getType(), e.getSpawnLocation(), e.getReason()).callEvent()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreaturePreSpawn(PreCreatureSpawnEvent e) {
        if (!new CreaturePreSpawnEvent(e.getType(), e.getSpawnLocation(), e.getReason()).callEvent()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTntPrime(TNTPrimeEvent e) {
        if (!new EntityPrimeTntEvent(e.getBlock(), EntityPrimeTntEvent.Reason.valueOf(e.getReason().name()), e.getPrimerEntity()).callEvent()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPortalDestroy(BlockDestroyEvent e) {
        if (!Config.Portal.ENABLED) {
            return;
        }

        if (e.getBlock().getType() != Material.NETHER_PORTAL && e.getBlock().getType() != Material.OBSIDIAN) {
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
