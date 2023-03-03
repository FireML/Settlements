package de.themoep.settlements.empire.events;

import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EmpireJoinEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Settlement settlement;
    private final Empire empire;

    public EmpireJoinEvent(Empire empire, Settlement settlement) {
        this.settlement = settlement;
        this.empire = empire;
    }

    public Empire getEmpire() {
        return empire;
    }

    public Settlement getSettlement() {
        return settlement;
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
