package de.themoep.settlements.land.events;


import de.themoep.settlements.settlement.Settlement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a landowner was successfully abandoned
 */
public class SettlementAbandonedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Settlement landOwner;

    public SettlementAbandonedEvent(Settlement landOwner) {
        this.landOwner = landOwner;
    }

    public Settlement getSettlement() {
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
