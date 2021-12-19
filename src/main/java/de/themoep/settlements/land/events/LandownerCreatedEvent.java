package de.themoep.settlements.land.events;

import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LandownerCreatedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final LandOwner<?, ?> landOwner;
    private final Resident resident;

    public LandownerCreatedEvent(LandOwner<?, ?> landOwner, Resident resident) {
        this.landOwner = landOwner;
        this.resident = resident;
    }

    public LandOwner<?, ?> getLandOwner() {
        return landOwner;
    }

    public Resident getResident() {
        return resident;
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
