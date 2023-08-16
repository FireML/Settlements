package de.themoep.settlements.integration;

import com.dre.brewery.BPlayer;
import com.dre.brewery.P;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.events.ChannelChatEvent;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BreweryIntegration implements Listener, ConfigMessages {
    private final P instance;

    public BreweryIntegration() {
        instance = P.p;
        SettlementsPlugin.getInstance().registerListener(this);
    }

    @EventHandler
    public void onChat(ChannelChatEvent e) {
        BPlayer p = BPlayer.get(e.getPlayer());
        int max = Utils.getMaxDrunkeness(e.getChannel());
        if (p != null && p.getDrunkeness() >= max) {
            e.setCancelled(true);
            getMessage("too-drunk-for-channel").tell(e.getPlayer(),
                    "channel", e.getChannel().getTag(),
                    "drunkeness", String.valueOf(p.getDrunkeness()),
                    "channel-drunkeness", String.valueOf(max),
                    "message", e.getMessage()
            );
        }
    }

    public P getBrewery() {
        return instance;
    }

    @Override
    public String type() {
        return "integration";
    }

    @Override
    public String name() {
        return instance.getName();
    }
}
