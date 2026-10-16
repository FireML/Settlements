package de.themoep.settlements.utils;

import de.themoep.settlements.SettlementsPlugin;
import uk.firedev.daisylib.events.move.PlayerMoveBlockEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import uk.firedev.daisylib.builders.TitleBuilder;
import uk.firedev.daisylib.messages.replacer.Replacer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
                        Map<String, Component> replacements = Map.of(
                                "seconds", ComponentUtils.parseComponent(String.valueOf(waiting.get())),
                                "action", ComponentUtils.parseComponent(action)
                        );
                        Replacer replacer = Replacer.replacer().addReplacements(replacements);
                        TitleBuilder.create()
                                .withTitle(replacer.apply(messages.getMessage("waiting.title").getComponent()))
                                .withSubtitle(replacer.apply(messages.getMessage("waiting.subtitle").getComponent()))
                                .withTimings(0, 20, 0)
                                .send(p);
                        ComponentUtils.sendActionBar(messages.getMessage("waiting.actionbar").getReplacedComponent(replacements), p);
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
            if (Message.get("move-cancelled.message").exists() && !Message.get("move-cancelled.message").getText().isEmpty()) {
                Message.get("move-cancelled.message").tell(e.getPlayer(), "action", action.getAction());
            }
            Map<String, Component> replacements = Map.of("action", ComponentUtils.parseComponent(action.getAction()));
            Replacer replacer = Replacer.replacer().addReplacements(replacements);
            TitleBuilder.create()
                    .withTitle(replacer.apply(Message.get("move-cancelled.title").getComponent()))
                    .withSubtitle(replacer.apply(Message.get("move-cancelled.subtitle").getComponent()))
                    .send(e.getPlayer());
            ComponentUtils.sendActionBar(Message.get("move-cancelled.actionbar").getReplacedComponent(replacements), e.getPlayer());
        }
    }

    synchronized public static ExecuteMoveUtil getInstance() {
        if (instance == null) {
            instance = new ExecuteMoveUtil();
        }
        return instance;
    }

    private abstract static class ActionRunnable extends BukkitRunnable {
        private final String action;

        public ActionRunnable(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }
    }
}
