package de.themoep.settlements.land.events;

import de.themoep.settlements.land.OwnedLand;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LandModeChangeEvent extends Event {

    private final OwnedLand land;

    public LandModeChangeEvent(OwnedLand land) {
        this.land = land;
    }

    private static final HandlerList handlers = new HandlerList();

    public OwnedLand getLand() {
        return this.land;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
