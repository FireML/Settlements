package de.themoep.settlements.land.events;

import de.themoep.settlements.land.OwnedLand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class LandEnterEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final OwnedLand to;

    public LandEnterEvent(OwnedLand to, Player player) {
        super(player);
        this.to = to;
    }

    public OwnedLand getTo() {
        return to;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
