package de.themoep.settlements.land.events.listeners;

import uk.firedev.daisylib.events.move.PlayerMoveChunkEvent;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.events.LandEnterEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class LandEnterListener implements Listener {

    @EventHandler
    public void onMoveChunkByWalk(PlayerMoveChunkEvent event) {
        OwnedLand from = LandManager.getInstance().getLand(event.getFrom());
        OwnedLand to = LandManager.getInstance().getLand(event.getTo());
        if (from == null && to != null) {
            Bukkit.getPluginManager().callEvent(new LandEnterEvent(to, event.getPlayer()));
        }
    }

    @EventHandler
    public void onMoveChunkByTeleport(PlayerTeleportEvent event) {
        OwnedLand from = LandManager.getInstance().getLand(event.getFrom());
        OwnedLand to = LandManager.getInstance().getLand(event.getTo());
        if (from == null && to != null) {
            Bukkit.getPluginManager().callEvent(new LandEnterEvent(to, event.getPlayer()));
        }
    }
}
