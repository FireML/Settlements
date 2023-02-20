package de.themoep.settlements.customevents;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.customevents.listeners.SpigotListener;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class CustomEventManager implements Listener {
    public CustomEventManager() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            SettlementsPlugin.getInstance().registerListener(
                    (Listener) Class.forName(getClass().getPackage().getName() + ".PaperListener").newInstance()
            );
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            SettlementsPlugin.getInstance().registerListener(new SpigotListener());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMoveBlock(PlayerMoveEvent e) {
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }
        final PlayerMoveBlockEvent pmbe = new PlayerMoveBlockEvent(e.getFrom(), e.getTo(), e.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(pmbe);
        if (pmbe.isCancelled()) {
            e.setCancelled(true);
        }
        if (e.getFrom().getBlockX() >> 4 == e.getTo().getBlockX() >> 4 && e.getFrom().getBlockZ() >> 4 == e.getTo().getBlockZ() >> 4) {
            return;
        }
        PlayerMoveChunkEvent pmce = new PlayerMoveChunkEvent(e.getFrom().getChunk(), e.getTo().getChunk(), e.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(pmce);
        if (pmce.isCancelled()) {
            e.setCancelled(true);
        }
    }
}
