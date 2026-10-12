package de.themoep.settlements.customevents.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import de.themoep.settlements.customevents.CreaturePreSpawnEvent;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.config.Config;
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
    
}
