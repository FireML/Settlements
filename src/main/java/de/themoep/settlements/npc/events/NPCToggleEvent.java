package de.themoep.settlements.npc.events;

import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NPCToggleEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final boolean newState;
    private final LandOwner<?, ?> landOwner;
    private final Resident resident;

    public NPCToggleEvent(boolean newState, Resident resident) {
        this.newState = newState;
        this.landOwner = resident.getSettlement();
        this.resident = resident;
    }

    public boolean getNewState() {
        return this.newState;
    }

    public LandOwner<?, ?> getLandOwner() {
        return this.landOwner;
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

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }
}
