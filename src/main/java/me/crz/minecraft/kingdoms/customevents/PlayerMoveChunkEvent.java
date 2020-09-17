package me.crz.minecraft.kingdoms.customevents;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerMoveChunkEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Chunk from;
    private Chunk to;
    private Player player;
    private boolean cancelled = false;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PlayerMoveChunkEvent(final Chunk from, final Chunk to, final Player player) {
        super();
        this.from = from;
        this.to = to;
        this.player = player;
    }

    public Chunk getFrom() {
        return from;
    }

    public Chunk getTo() {
        return to;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(final boolean cancel) {
        cancelled = cancel;
    }
}
