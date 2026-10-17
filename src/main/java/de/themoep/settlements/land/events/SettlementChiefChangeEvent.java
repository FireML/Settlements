package de.themoep.settlements.land.events;


import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a landowner chief was changed
 */
public class SettlementChiefChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Settlement landOwner;

    private final Resident oldChief;
    private final Resident newChief;

    public SettlementChiefChangeEvent(Settlement landOwner, Resident oldChief, Resident newChief) {
        this.landOwner = landOwner;
        this.oldChief = oldChief;
        this.newChief = newChief;
    }

    public Settlement getSettlement() {
        return landOwner;
    }

    public Resident getOldChief() {
        return oldChief;
    }

    public Resident getNewChief() {
        return newChief;
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
