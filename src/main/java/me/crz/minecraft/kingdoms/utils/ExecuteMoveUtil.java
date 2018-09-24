package me.crz.minecraft.kingdoms.utils;

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

    public void execute(final Player p, final Runnable r, final int seconds, final String action) {
        uuids.put(p.getUniqueId(), action);
        ThreadManager.getInstance().execute(() -> {
            if (uuids.containsKey(p.getUniqueId())) {
                r.run();
                uuids.remove(p.getUniqueId());
            }
        }, seconds);
    }

    @EventHandler
    public void on(final PlayerMoveBlockEvent e) {
        if (uuids.containsKey(e.getPlayer().getUniqueId())) {
            MessageUtils.tell(e.getPlayer(), "&c" + uuids.get(e.getPlayer().getUniqueId()) + " cancled because you moved!");
            uuids.remove(e.getPlayer().getUniqueId());
        }
    }

    public static ExecuteMoveUtil getInstance() {
        if (instance == null) {
            instance = new ExecuteMoveUtil();
        }
        return instance;
    }
}
