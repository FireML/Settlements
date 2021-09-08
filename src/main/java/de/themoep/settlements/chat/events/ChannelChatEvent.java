package de.themoep.settlements.chat.events;

import de.themoep.settlements.chat.ChatChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collections;

public class ChannelChatEvent extends AsyncPlayerChatEvent {
    private final ChatChannel channel;

    public ChannelChatEvent(boolean async, Player who, String message, ChatChannel channel) {
        super(async, who, message, Collections.singleton(who));
        this.channel = channel;
    }

    public ChatChannel getChannel() {
        return channel;
    }
}
