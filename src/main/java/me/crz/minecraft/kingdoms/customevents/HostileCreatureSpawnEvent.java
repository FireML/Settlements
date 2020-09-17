package me.crz.minecraft.kingdoms.customevents;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityEvent;

public class HostileCreatureSpawnEvent extends EntityEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Location location;
    private final CreatureSpawnEvent.SpawnReason spawnReason;
    private boolean canceled = false;
    
    public HostileCreatureSpawnEvent(LivingEntity entity, Location location, CreatureSpawnEvent.SpawnReason spawnReason) {
        super(entity);
        this.location = location;
        this.spawnReason = spawnReason;
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

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
