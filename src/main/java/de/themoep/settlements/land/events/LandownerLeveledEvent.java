package de.themoep.settlements.land.events;

import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.LandownerLevel;
import de.themoep.settlements.resident.Resident;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called after a landowner achieved a new level
 */
public class LandownerLeveledEvent extends Event {
    private final static HandlerList handlerList = new HandlerList();
    private final LandOwner<?, ? extends LandownerLevel> leveled;

    private final Resident resident;

    public LandownerLeveledEvent(@NotNull LandOwner<?, ? extends LandownerLevel> leveled, @Nullable Resident resident) {
        this.leveled = leveled;
        this.resident = resident;
    }
    public @NotNull LandOwner<?, ? extends LandownerLevel> getLeveled() {
        return leveled;
    }

    public @Nullable Resident getResident() {
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
