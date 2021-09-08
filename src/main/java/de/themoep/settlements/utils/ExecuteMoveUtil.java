package de.themoep.settlements.utils;

import de.themoep.settlements.customevents.PlayerMoveBlockEvent;
import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ExecuteMoveUtil implements Listener {
    private static ExecuteMoveUtil instance;
    Map<UUID, ActionRunnable> scheduled = new HashMap<>();

    public ExecuteMoveUtil() {
        super();
        SettlementsPlugin.getInstance().registerListener(this);
    }

    /**
     * @return true if a waiting task was scheduled
     */
    public boolean execute(Player p, Runnable r, int delay, String action, ConfigMessages messages) {
        if (delay > 0) {
            AtomicInteger waiting = new AtomicInteger(delay);
            ActionRunnable waitingRunnable = new ActionRunnable(action) {
                @Override
                public void run() {
                    if (waiting.get() > 0) {
                        new TitleBuilder()
                                .withTitle(messages.getMessage("waiting.title"))
                                .withSubtitle(messages.getMessage("waiting.subtitle"))
                                .withActionbar(messages.getMessage("waiting.actionbar"))
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
            waitingRunnable.runTaskTimer(SettlementsPlugin.getInstance(), 20, 20);
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
                    .withTitle(Message.get("move-cancelled.title"))
                    .withSubtitle(Message.get("move-cancelled.subtitle"))
                    .withActionbar(Message.get("move-cancelled.actionbar"))
                    .send(e.getPlayer(), "action", action.getAction());
        }
    }

    synchronized public static ExecuteMoveUtil getInstance() {
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
