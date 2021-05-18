package me.crz.minecraft.kingdoms.land.events;

import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.resident.Resident;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LandownerDisbandEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final LandOwner landOwner;

    public LandownerDisbandEvent(LandOwner landOwner) {
        this.landOwner = landOwner;
    }

    public LandOwner getLandOwner() {
        return landOwner;
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
