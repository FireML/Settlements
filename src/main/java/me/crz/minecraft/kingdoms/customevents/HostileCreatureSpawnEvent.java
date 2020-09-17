package me.crz.minecraft.kingdoms.customevents;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class HostileCreatureSpawnEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private LivingEntity entity;
    private EntityType entityType;
    private Location location;
    private CreatureSpawnEvent.SpawnReason spawnReason;
    private CreatureSpawnEvent e;
    private boolean canceled;
    
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public HostileCreatureSpawnEvent(final LivingEntity entity, final EntityType entityType, final Location location, final CreatureSpawnEvent.SpawnReason spawnReason, final CreatureSpawnEvent e) {
        super();
        this.entity = entity;
        this.entityType = entityType;
        this.location = location;
        this.spawnReason = spawnReason;
        this.e = e;
    }
    
    public LivingEntity getEntity() {
        return entity;
    }
    
    public EntityType getEntityType() {
        return entityType;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public CreatureSpawnEvent.SpawnReason getSpawnReason() {
        return spawnReason;
    }
    
    public boolean isCancelled() {
        return canceled;
    }
    
    public void setCancelled(final boolean cancel) {
        canceled = cancel;
    }

    public CreatureSpawnEvent getBukkitEvent() {
        return e;
    }
}
