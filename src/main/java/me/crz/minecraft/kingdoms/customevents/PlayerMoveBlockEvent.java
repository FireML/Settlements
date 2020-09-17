package me.crz.minecraft.kingdoms.customevents;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerMoveBlockEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Location from;
    private Location to;
    private Player player;
    private boolean canceled = false;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerMoveBlockEvent(final Location from, final Location to, final Player player) {
        super();
        this.from = from;
        this.to = to;
        this.player = player;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isCancelled() {
        return canceled;
    }

    public void setCancelled(final boolean cancel) {
        canceled = cancel;
    }
}
