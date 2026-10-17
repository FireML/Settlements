package de.themoep.settlements.land.events;


import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SettlementCreatedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Settlement landOwner;
    private final Resident resident;

    public SettlementCreatedEvent(Settlement landOwner, Resident resident) {
        this.landOwner = landOwner;
        this.resident = resident;
    }

    public Settlement getSettlement() {
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
