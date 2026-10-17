package de.themoep.settlements.land.events;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a landowner achieved a new level
 */
public class SettlementLeveledEvent extends Event {
    private final static HandlerList handlerList = new HandlerList();
    private final Settlement leveled;

    private final Resident resident;

    public SettlementLeveledEvent(@NotNull Settlement leveled, @NotNull Resident resident) {
        this.leveled = leveled;
        this.resident = resident;
    }

    public @NotNull Settlement getSettlement() {
        return leveled;
    }

    public @NotNull Resident getResident() {
        return resident;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
