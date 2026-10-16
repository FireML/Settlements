package de.themoep.settlements.customevents;

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CustomEventManager implements Listener {

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

}
