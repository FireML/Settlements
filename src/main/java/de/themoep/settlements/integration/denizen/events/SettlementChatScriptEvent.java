package de.themoep.settlements.integration.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.chat.events.ChannelChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SettlementChatScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // settlements player chats
    //
    // @Cancellable false
    //
    // @Triggers when a player chats
    //
    // @Context
    // <context.message> returns the message as an ElementTag
    //
    // @Plugin Settlements
    //
    // @Player Always.
    //
    // @Group Settlements
    //
    // -->

    public Player player;
    public String message;
    public ChatChannel channel;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("settlements player chats");
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("player")) {
            return new PlayerTag(player);
        }
        if (name.equals("message")) {
            return new ElementTag(message);
        }
        if (name.equals("channel")) {
            return new ElementTag(channel);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onSettlementChatEvent(ChannelChatEvent event) {
        this.player = event.getPlayer();
        this.message = event.getMessage();
        this.channel = event.getChannel();
        fire(event);
    }
}
