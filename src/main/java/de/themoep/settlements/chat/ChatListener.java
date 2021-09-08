package de.themoep.settlements.chat;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;

public class ChatListener implements Listener {

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!Config.Chat.ENABLED) {
            return;
        }
        String[] parts = event.getBuffer().split(" ");
        String last = parts[parts.length - 1];
        if (last.startsWith("@")) {
            event.setCompletions(new ArrayList<>(event.getCompletions()));
            String namePart = last.substring(1);
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p != event.getSender() && !((event.getSender() instanceof Player) || ((Player) event.getSender()).canSee(p)))
                    .map(HumanEntity::getName)
                    .filter(n -> n.toLowerCase().startsWith(namePart.toLowerCase()))
                    .sorted(String::compareToIgnoreCase)
                    .forEachOrdered(n -> event.getCompletions().add("@" + n));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Resident res = ResidentManager.getInstance().getResident(event.getPlayer()); // should be cached
        Utils.runNextTick(() -> {
            boolean activeChanged = false;
            for (ChatChannel channel : ChatChannel.values()) {
                if (channel != ChatChannel.GENERAL) {
                    if (channel.autoJoin(event.getPlayer())) {
                        res.getChannels().add(channel);
                    } else if (Config.Chat.RESET_CHANNELS_ON_LOGIN) {
                        res.getChannels().remove(channel);
                        if (res.getActiveChannel() == channel) {
                            activeChanged = res.setActiveChannel(res.getChannels().iterator().next());
                        }
                    }
                }
            }
            if (activeChanged) {
                Message.get("command.chat.active").tell(res, "name", res.getActiveChannel().getTag());
            }
        });
    }
}
