package me.crz.minecraft.kingdoms.customevents;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PassiveCreatureDamageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Entity entity;
    private EntityType entityType;
    private EntityDamageByEntityEvent e;
    private Player damager;
    private boolean canceled;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PassiveCreatureDamageEvent(final Entity entity, final EntityType entityType, final Player damager, final EntityDamageByEntityEvent e) {
        super();
        this.entity = entity;
        this.entityType = entityType;
        this.e = e;
        this.damager = damager;
    }

    public Entity getEntity() {
        return entity;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Player getDamager() {
        return damager;
    }

    public boolean isCancelled() {
        return canceled;
    }

    public void setCancelled(final boolean cancel) {
        canceled = cancel;
    }

    public EntityDamageByEntityEvent getBukkitEvent() {
        return e;
    }
}
