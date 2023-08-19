package de.themoep.settlements.land.events;

import de.themoep.settlements.land.OwnedLand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

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
