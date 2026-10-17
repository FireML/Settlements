package de.themoep.settlements.land.events;


import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.settlement.Settlement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a land was claimed
 */
public class LandPreClaimEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final OwnedLand land;

    public LandPreClaimEvent(OwnedLand land) {
        this.land = land;
    }

    public Settlement getSettlement() {
        return land.getSettlement();
    }

    public OwnedLand getLand() {
        return land;
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
