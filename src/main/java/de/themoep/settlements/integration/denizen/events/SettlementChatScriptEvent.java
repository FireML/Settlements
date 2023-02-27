package de.themoep.settlements.integration.denizen.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import de.themoep.settlements.chat.events.ChannelChatEvent;
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
    // @Player Always.
    //
    // @Plugin Settlements
    //
    // @Group Settlements
    //
    // -->

    public ChannelChatEvent event;

    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("settlements player chats");
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("message")) {
            return new ElementTag(event.getMessage());
        }
        if (name.equals("channel")) {
            return new ElementTag(event.getChannel());
        }
        return super.getContext(name);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(new PlayerTag(event.getPlayer()), null);
    }

    @EventHandler
    public void onSettlementChat(ChannelChatEvent event) {
        this.event = event;
        fire(event);
    }
}
