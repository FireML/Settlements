package de.themoep.settlements.village.events;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SettlementJoinEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Village settlement;
    private final Resident resident;

    public SettlementJoinEvent(Village settlement, Resident resident) {
        this.settlement = settlement;
        this.resident = resident;
    }

    public Village getSettlement() {
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
