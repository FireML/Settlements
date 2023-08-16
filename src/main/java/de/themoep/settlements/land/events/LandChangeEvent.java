package de.themoep.settlements.land.events;

import de.themoep.settlements.land.OwnedLand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class LandChangeEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final OwnedLand to;
    private final OwnedLand from;

    public LandChangeEvent(OwnedLand to, OwnedLand from, Player player) {
        super(player);
        this.to = to;
        this.from = from;
    }

    public OwnedLand getTo() { return to; }

    public OwnedLand getFrom() { return from; }

    public @NotNull HandlerList getHandlers() { return handlers; }

    public static HandlerList getHandlerList() { return handlers; }
}
