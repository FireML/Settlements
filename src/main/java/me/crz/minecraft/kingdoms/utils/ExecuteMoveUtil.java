package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveBlockEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import me.crz.minecraft.kingdoms.Base;
import org.bukkit.scheduler.BukkitRunnable;

public class ExecuteMoveUtil extends Base {
    private static ExecuteMoveUtil instance;
    Map<UUID, ActionRunnable> scheduled = new HashMap<>();

    public ExecuteMoveUtil() {
        super();
        Base.registerListener(this);
    }

    /**
     * @return true if a waiting task was scheduled
     */
    public boolean execute(Player p, Runnable r, int delay, String action, String messagePath) {
        if (delay > 0) {
            AtomicInteger waiting = new AtomicInteger(delay);
            ConfigMessages messages = new PathedConfigMessages(messagePath);
            ActionRunnable waitingRunnable = new ActionRunnable(action) {
                @Override
                public void run() {
                    if (waiting.get() > 0) {
                        new TitleBuilder()
                                .withTitle(messages.getMessage("waiting.title").getText())
                                .withSubtitle(messages.getMessage("waiting.subtitle").getText())
                                .withActionbar(messages.getMessage("waiting.actionbar").getText())
                                .withTimings(0, 21, 0)
                                .send(p,
                                        "seconds", String.valueOf(waiting.get()),
                                        "action", action
                                );
                    } else {
                        ActionRunnable waiting = scheduled.remove(p.getUniqueId());
                        if (waiting != null && waiting.getTaskId() == getTaskId() && p.isOnline()) {
                            r.run();
                        }
                        cancel();
                    }
                    waiting.getAndDecrement();
                }
            };
            scheduled.put(p.getUniqueId(), waitingRunnable);
            waitingRunnable.run();
            waitingRunnable.runTaskTimer(KingdomsPlugin.getInstance(), 20, 20);
            return true;
        } else {
            r.run();
            return false;
        }
    }

    @EventHandler
    public void on(PlayerMoveBlockEvent e) {
        ActionRunnable action = scheduled.remove(e.getPlayer().getUniqueId());
        if (action != null) {
            action.cancel();
            if (Message.get("move-cancelled.message").exists()) {
                Message.get("move-cancelled.message").tell(e.getPlayer(), "action", action.getAction());
            }
            new TitleBuilder()
                    .withTitle(Message.get("move-cancelled.title").getText())
                    .withSubtitle(Message.get("move-cancelled.subtitle").getText())
                    .withActionbar(Message.get("move-cancelled.actionbar").getText())
                    .send(e.getPlayer(), "action", action.getAction());
        }
    }

    public static ExecuteMoveUtil getInstance() {
        if (instance == null) {
            instance = new ExecuteMoveUtil();
        }
        return instance;
    }

    private abstract class ActionRunnable extends BukkitRunnable {
        private final String action;

        public ActionRunnable(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }
    }
}
