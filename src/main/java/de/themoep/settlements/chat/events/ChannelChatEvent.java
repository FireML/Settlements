package de.themoep.settlements.chat.events;

import de.themoep.settlements.chat.ChatChannel;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import uk.firedev.poleislib.utils.ComponentUtils;

public class ChannelChatEvent extends Event implements Cancellable {
    private final ChatChannel channel;
    private final Player player;
    private final Component message;

    public ChannelChatEvent(Player who, Component message, ChatChannel channel) {
        this.channel = channel;
        this.message = message;
        this.player = who;
    }

    public ChatChannel getChannel() { return channel; }
    public Player getPlayer() { return player; }
    public Component getMessage() { return message; }
    public String getStringMessage() { return ComponentUtils.toString(message); }
    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() { return handlers; }
}
