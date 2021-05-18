package me.crz.minecraft.kingdoms.land.events;

import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.resident.Resident;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LandownerChiefChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final LandOwner landOwner;

    private final Resident oldChief;
    private final Resident newChief;

    public LandownerChiefChangeEvent(LandOwner landOwner, Resident oldChief, Resident newChief) {
        this.landOwner = landOwner;
        this.oldChief = oldChief;
        this.newChief = newChief;
    }

    public LandOwner getLandOwner() {
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
