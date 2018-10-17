package me.crz.minecraft.kingdoms.scoreboard.listeners;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.scoreboard.CScoreBoard;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import me.crz.minecraft.kingdoms.scoreboard.TabHeader;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ScoreboardListeners implements Listener {
    private final ScoreboardManager manager;
    private TabHeader th;

    public ScoreboardListeners(ScoreboardManager manager) {
        super();
        th = new TabHeader(manager.getMessage("tab.header"), manager.getMessage("tab.footer"));
        this.manager = manager;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KingdomsPlugin.getInstance(), this::onUpdate, 0L, 200L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KingdomsPlugin.getInstance(), this::onUpdateRainbowTag, 0L, 5L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            th.send(p,
                    "amount", String.valueOf(Bukkit.getOnlinePlayers().size()),
                    "world", p.getWorld().getName()
            );
        }
        if (manager.getScoreboardPolicy() == null) {
            return;
        }
        Player p = e.getPlayer();
        manager.getScoreboards().put(p.getUniqueId(), new CScoreBoard(p, manager));
        manager.update();
    }

    @EventHandler
    public void on(PlayerChangedWorldEvent e) {
        th.send(e.getPlayer(),
                "amount", String.valueOf(Bukkit.getOnlinePlayers().size()),
                "world", e.getPlayer().getWorld().getName()
        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            th.send(p,
                    "amount", String.valueOf(Bukkit.getOnlinePlayers().size()),
                    "world", p.getWorld().getName()
            );
        }
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
