package me.crz.minecraft.kingdoms.scoreboard.listeners;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.scoreboard.CScoreBoard;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
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
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KingdomsPlugin.getInstance(), this::onUpdate, 0L, 200L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KingdomsPlugin.getInstance(), this::onUpdateRainbowTag, 0L, 5L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (manager.getScoreboardPolicy() == null) {
            return;
        }
        Player p = e.getPlayer();
        manager.getScoreboards().put(p.getUniqueId(), new CScoreBoard(p, manager));
        manager.update();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (manager.getScoreboards().containsKey(e.getPlayer().getUniqueId())) {
            manager.getScoreboards().get(e.getPlayer().getUniqueId()).unregister();
        }
        manager.getScoreboards().remove(e.getPlayer().getUniqueId());
    }

    public void onUpdate() {
        manager.update();
    }

    public void onUpdateRainbowTag() {
        if (manager.getRainbowTags().isEmpty()) {
            return;
        }
        for (CScoreBoard scoreboard : manager.getScoreboards().values()) {
            scoreboard.updateTeams();
        }
    }
}
