package me.crz.minecraft.kingdoms.customevents;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityEvent;

public class PassiveCreatureDamageEvent extends EntityEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final EntityDamageByEntityEvent e;
    private final Player damager;
    private boolean canceled = false;

    public PassiveCreatureDamageEvent(Entity entity, Player damager, EntityDamageByEntityEvent e) {
        super(entity);
        this.e = e;
        this.damager = damager;
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

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
