package me.crz.minecraft.kingdoms.customevents;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class HostileCreaturePreSpawnEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private EntityType entityType;
    private Location location;
    private CreatureSpawnEvent.SpawnReason spawnReason;
    private PreCreatureSpawnEvent e;
    private boolean canceled;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HostileCreaturePreSpawnEvent(final EntityType entityType, final Location location, final CreatureSpawnEvent.SpawnReason spawnReason, final PreCreatureSpawnEvent e) {
        super();
        this.entityType = entityType;
        this.location = location;
        this.spawnReason = spawnReason;
        this.e = e;
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

    public PreCreatureSpawnEvent getBukkitEvent() {
        return e;
    }
}
