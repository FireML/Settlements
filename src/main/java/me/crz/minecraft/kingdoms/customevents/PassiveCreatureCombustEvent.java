package me.crz.minecraft.kingdoms.customevents;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustByEntityEvent;

public class PassiveCreatureCombustEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Entity entity;
    private EntityType entityType;
    private EntityCombustByEntityEvent e;
    private Player combuster;
    private boolean canceled;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PassiveCreatureCombustEvent(final Entity entity, final EntityType entityType, final Player combuster, final EntityCombustByEntityEvent e) {
        super();
        this.entity = entity;
        this.entityType = entityType;
        this.e = e;
        this.combuster = combuster;
    }

    public Entity getEntity() {
        return entity;
    }

    public EntityType getEntityType() {
        return entityType;
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

    public EntityCombustByEntityEvent getBukkitEvent() {
        return e;
    }
}
