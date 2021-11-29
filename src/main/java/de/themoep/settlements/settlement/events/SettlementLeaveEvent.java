package de.themoep.settlements.settlement.events;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SettlementLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Settlement settlement;
    private final Resident resident;

    public SettlementLeaveEvent(Settlement settlement, Resident resident) {
        this.settlement = settlement;
        this.resident = resident;
    }

    public Settlement getSettlement() {
        return settlement;
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
