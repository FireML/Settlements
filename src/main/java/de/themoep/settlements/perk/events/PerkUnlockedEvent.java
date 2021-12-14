package de.themoep.settlements.perk.events;

import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandownerLevel;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.resident.Resident;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PerkUnlockedEvent extends Event {
    private final static HandlerList handlerList = new HandlerList();
    private final LandOwner<?, ? extends LandownerLevel> landOwner;
    private final Resident resident;
    private final Perk perk;

    public PerkUnlockedEvent(LandOwner<?, ? extends LandownerLevel> landOwner, Resident resident, Perk perk) {
        this.landOwner = landOwner;
        this.resident = resident;
        this.perk = perk;
    }

    public LandOwner<?, ? extends LandownerLevel> getLandOwner() {
        return landOwner;
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
