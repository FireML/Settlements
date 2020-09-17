package me.crz.minecraft.kingdoms.customevents;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public class PassiveCreatureCombustEvent extends EntityEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final Player combuster;
    private boolean canceled = false;

    public PassiveCreatureCombustEvent(Entity entity, Player combuster) {
        super(entity);
        this.entity = entity;
        this.combuster = combuster;
    }

    public Player getCombuster() {
        return combuster;
    }

    public boolean isCancelled() {
        return canceled;
    }

    public void setCancelled(final boolean cancel) {
        canceled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
