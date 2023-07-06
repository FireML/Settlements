package de.themoep.settlements.customevents.listeners;

import de.themoep.settlements.customevents.CreaturePreSpawnEvent;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;

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
