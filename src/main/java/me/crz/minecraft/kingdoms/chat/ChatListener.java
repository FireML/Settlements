package me.crz.minecraft.kingdoms.chat;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;

public class ChatListener implements Listener {

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
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
}
