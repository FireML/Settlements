package me.crz.minecraft.kingdoms.village.events;

import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SettlementLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Village settlement;
    private final Resident resident;

    public SettlementLeaveEvent(Village settlement, Resident resident) {
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
