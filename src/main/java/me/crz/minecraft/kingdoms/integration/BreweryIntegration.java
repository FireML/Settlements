package me.crz.minecraft.kingdoms.integration;

import com.dre.brewery.BPlayer;
import com.dre.brewery.P;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.events.ChannelChatEvent;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BreweryIntegration implements Listener, ConfigMessages {
    private P instance;

    public BreweryIntegration() {
        instance = P.p;
        KingdomsPlugin.getInstance().registerListener(this);
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
