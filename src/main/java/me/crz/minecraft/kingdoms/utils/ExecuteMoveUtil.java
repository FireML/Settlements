package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveBlockEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

import me.crz.minecraft.kingdoms.Base;

public class ExecuteMoveUtil extends Base {
    private static ExecuteMoveUtil instance;
    Map<UUID, String> uuids;

    public ExecuteMoveUtil() {
        super();
        uuids = new HashMap<>();
        Base.registerListener(this);
    }

    public void execute(Player p, Runnable r, int seconds, String action) {
        uuids.put(p.getUniqueId(), action);
        Bukkit.getScheduler().runTaskLater(KingdomsPlugin.getInstance(), () -> {
            if (uuids.remove(p.getUniqueId(), action) && p.isOnline()) {
                r.run();
            }
        }, seconds * 20);
    }

    @EventHandler
    public void on(PlayerMoveBlockEvent e) {
        if (uuids.remove(e.getPlayer().getUniqueId()) != null) {
            MessageUtils.tell(e.getPlayer(), "&c" + uuids.get(e.getPlayer().getUniqueId()) + " cancled because you moved!");
        }
    }

    public static ExecuteMoveUtil getInstance() {
        if (instance == null) {
            instance = new ExecuteMoveUtil();
        }
        return instance;
    }
}
