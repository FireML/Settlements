package me.crz.minecraft.kingdoms.scoreboard;

import co.aikar.timings.Timing;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

import static me.crz.minecraft.kingdoms.utils.Utils.trim;

public class CScoreBoard {
    private final ScoreboardManager manager;

    private Player p;
    private Scoreboard scoreboard;
    private Objective sidebarObjective;
    private Objective undernameObjective;
    private Objective tablistObjective;
    private List<String> sidebar = new ArrayList<>();

    public CScoreBoard(Player p, ScoreboardManager manager) {
        super();
        this.manager = manager;
        this.p = p;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public void update() {
        try (Timing ignored = KingdomsPlugin.getInstance().getAndStartTiming("Scoreboard Update Sidebar")) {
            updateSidebar();
        }
    }

    public void updateSidebar() {
        List<String> sidebarText = manager.getCustomLines() == null && manager.getUpdateRate() > 0 ? manager.getScoreboardPolicy().getSidebar(p) : manager.getCustomLines();
        if (sidebarText == null) {
            if (sidebarObjective != null) {
                sidebarObjective.unregister();
                sidebarObjective = null;
            }
            return;
        }
        setSidebar(sidebarText);
    }

    public void setSidebar(List<String> sidebar) {
        Scoreboard scoreboard = getScoreboard();
        if (sidebarObjective == null || sidebarObjective.getScoreboard() != scoreboard) {
            sidebarObjective = scoreboard.registerNewObjective("title", "dummy", "title");
            sidebarObjective.setDisplayName(manager.getTitle());
            sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        List<String> set = new ArrayList<>();
        if (sidebar != null && sidebar.size() > 0) {
            int score = 15;
            for (String line : sidebar) {
                line = trim(line, 40);
                if (score < 1) {
                    break;
                }
                if (set.contains(line)) {
                    continue;
                }
                set.add(line);
                sidebarObjective.getScore(line).setScore(score);
                --score;
            }
        }
        for (String last : this.sidebar) {
            if (!set.contains(last)) {
                scoreboard.resetScores(last);
            }
        }
        this.sidebar = set;
    }

    public void unregister() {
        if (sidebarObjective != null) {
            sidebarObjective.unregister();
        }
        if (undernameObjective != null) {
            undernameObjective.unregister();
        }
        if (tablistObjective != null) {
            tablistObjective.unregister();
        }
        sidebarObjective = null;
        undernameObjective = null;
        tablistObjective = null;
    }

    public void setTitle(String title) {
        if (sidebarObjective != null) {
            sidebarObjective.setDisplayName(title);
        }
    }

    public Player getPlayer() {
        return p;
    }

    public Scoreboard getScoreboard() {
        if (isUsedByMultiplePlayers(getPlayer(), getPlayer().getScoreboard())) {
            getPlayer().setScoreboard(scoreboard);
        }
        return getPlayer().getScoreboard();
    }

    private boolean isUsedByMultiplePlayers(Player player, Scoreboard scoreboard) {
        if (scoreboard == this.scoreboard) {
            return false;
        }
        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            return true;
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer != player && onlinePlayer.getScoreboard() == scoreboard) {
                return true;
            }
        }
        return false;
    }
}
