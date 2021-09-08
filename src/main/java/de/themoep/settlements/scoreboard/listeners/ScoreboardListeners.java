package de.themoep.settlements.scoreboard.listeners;

import de.themoep.settlements.scoreboard.CScoreBoard;
import de.themoep.settlements.scoreboard.ScoreboardManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardListeners implements Listener {
    private final ScoreboardManager manager;

    public ScoreboardListeners(ScoreboardManager manager) {
        super();
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (manager.getScoreboardPolicy() == null) {
            return;
        }
        Player p = e.getPlayer();
        CScoreBoard scoreboard = new CScoreBoard(p, manager);
        manager.getScoreboards().put(p.getUniqueId(), scoreboard);
        Utils.runSync(scoreboard::update);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (manager.getScoreboards().containsKey(e.getPlayer().getUniqueId())) {
            manager.getScoreboards().get(e.getPlayer().getUniqueId()).unregister();
        }
        manager.getScoreboards().remove(e.getPlayer().getUniqueId());
    }
}
