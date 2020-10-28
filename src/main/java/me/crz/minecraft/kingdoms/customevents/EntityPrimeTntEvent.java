package me.crz.minecraft.kingdoms.customevents;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class EntityPrimeTntEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Reason reason;
    private final Entity entity;
    private boolean cancelled = false;

    public EntityPrimeTntEvent(Block block, Reason reason, Entity entity) {
        super(block);
        this.reason = reason;
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public Reason getReason() {
        return reason;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(final boolean cancel) {
        cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum Reason {
        EXPLOSION,
        FIRE,
        ITEM,
        PROJECTILE,
        REDSTONE
    }
}
