package de.themoep.settlements.land.events;

import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a land was successfully claimed
 */
public class LandClaimedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final OwnedLand land;

    public LandClaimedEvent(OwnedLand land) {
        this.land = land;
    }

    public LandOwner<?, ?> getLandOwner() {
        return land.getLandOwner();
    }

    public OwnedLand getLand() {
        return land;
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
