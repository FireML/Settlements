package de.themoep.settlements.land.events;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.customevents.PlayerMoveChunkEvent;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;

public class LandLeaveEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final OwnedLand from;

    public LandLeaveEvent(OwnedLand from, Player player) {
        super(player);
        this.from = from;
    }

    public OwnedLand getFrom() {
        return from;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
