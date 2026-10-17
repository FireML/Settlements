package de.themoep.settlements.perk.events;


import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PerkUnlockedEvent extends Event {
    private final static HandlerList handlerList = new HandlerList();
    private final Settlement settlement;
    private final Resident resident;
    private final Perk perk;

    public PerkUnlockedEvent(Settlement settlement, Resident resident, Perk perk) {
        this.settlement = settlement;
        this.resident = resident;
        this.perk = perk;
    }

    public Settlement getSettlement() {
            return settlement;
    }

    public Resident getResident() {
        return resident;
    }

    public Perk getPerk() {
        return perk;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
