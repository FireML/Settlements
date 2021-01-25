package me.crz.minecraft.kingdoms.customevents;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.customevents.listeners.SpigotListener;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class CustomEventManager implements Listener {
    public CustomEventManager() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            KingdomsPlugin.getInstance().registerListener(
                    (Listener) Class.forName(getClass().getPackage().getName() + ".PaperListener").newInstance()
            );
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            KingdomsPlugin.getInstance().registerListener(new SpigotListener());
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
