package me.crz.minecraft.kingdoms.scoreboard.listeners;

import me.crz.minecraft.kingdoms.scoreboard.commands.CommandRainbowtag;
import me.crz.minecraft.kingdoms.utils.Message;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.EventHandler;

import me.crz.minecraft.kingdoms.scoreboard.CScoreBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.scoreboard.TabHeader;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import org.bukkit.event.Listener;

public class ScoreboardListeners implements Listener {
    private ScoreboardManager scoreboardManager;
    private TabHeader th;

    public ScoreboardListeners(final ScoreboardManager scoreboardManager) {
        super();
        th = new TabHeader(Message.get("server-name").getText(), "&fThere are &6" + Bukkit.getOnlinePlayers().size() + "&f Players online!");
        this.scoreboardManager = scoreboardManager;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KingdomsPlugin.getInstance(), this::onUpdate, 0L, 200L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(KingdomsPlugin.getInstance(), this::onUpdateRainbowTag, 0L, 5L);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
        for (final Player p : Bukkit.getOnlinePlayers()) {
            th.setFooter("&fThere are &6" + Bukkit.getOnlinePlayers().size() + "&f Players online!" + "\n&fWorld: &6" + p.getWorld().getName());
            th.send(p);
        }
        if (scoreboardManager.getScoreboardPolicy() == null) {
            return;
        }
        Player p = e.getPlayer();
        scoreboardManager.getScoreboards().put(p.getUniqueId(), new CScoreBoard(p));
        scoreboardManager.update();
    }

    @EventHandler
    public void on(final PlayerChangedWorldEvent e) {
        th.setFooter("&fThere are &6" + Bukkit.getOnlinePlayers().size() + "&f Players online!" + "\n&fWorld: &6" + e.getPlayer().getWorld().getName());
        th.send(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        for (final Player p : Bukkit.getOnlinePlayers()) {
            th.setFooter("&fThere are &6" + Bukkit.getOnlinePlayers().size() + "&f Players online!" + "\n&fWorld: &6" + p.getWorld().getName());
            th.send(p);
        }
        if (scoreboardManager.getScoreboards().containsKey(e.getPlayer().getUniqueId())) {
            scoreboardManager.getScoreboards().get(e.getPlayer().getUniqueId()).unregister();
        }
        scoreboardManager.getScoreboards().remove(e.getPlayer().getUniqueId());
    }

    public void onUpdate() {
        scoreboardManager.update();
    }

    public void onUpdateRainbowTag() {
        if (CommandRainbowtag.getInstance().getEnabled().size() < 1) {
            return;
        }
        for (final CScoreBoard scoreboard : scoreboardManager.getScoreboards().values()) {
            scoreboard.updateTeams();
        }
    }
}
