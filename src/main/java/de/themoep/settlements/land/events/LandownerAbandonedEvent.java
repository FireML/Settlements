package de.themoep.settlements.land.events;

import de.themoep.settlements.land.LandOwner;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a landowner was successfully abandoned
 */
public class LandownerAbandonedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final LandOwner<?, ?> landOwner;

    public LandownerAbandonedEvent(LandOwner<?, ?> landOwner) {
        this.landOwner = landOwner;
    }

    public LandOwner<?, ?> getLandOwner() {
        return landOwner;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
