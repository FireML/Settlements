package de.themoep.settlements.scoreboard;

import co.aikar.timings.lib.MCTiming;
import de.themoep.settlements.SettlementsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

import java.util.Collections;

import static de.themoep.settlements.utils.Utils.trim;

public class CScoreBoard {
    private final ScoreboardManager manager;

    private Player p;
    private Scoreboard scoreboard;
    private Objective sidebarObjective;
    private Objective undernameObjective;
    private Objective tablistObjective;
    private List<String> sidebar = Collections.emptyList();

    public CScoreBoard(Player p, ScoreboardManager manager) {
        super();
        this.manager = manager;
        this.p = p;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public void update() {
        try (MCTiming ignored = SettlementsPlugin.getInstance().getAndStartTiming("Scoreboard Update Sidebar")) {
            updateSidebar();
        }
    }

    public void updateSidebar() {
        List<String> sidebarText = manager.getCustomLines() == null && manager.getUpdateRate() > 0 ? manager.getScoreboardPolicy().getSidebar(p) : new ArrayList<>(manager.getCustomLines());
        if (sidebarText == null) {
            if (sidebarObjective != null) {
                sidebarObjective.unregister();
                sidebarObjective = null;
            }
            this.sidebar = Collections.emptyList();
            return;
        }
        setSidebar(sidebarText);
    }

    public void setSidebar(List<String> sidebar) {
        Scoreboard scoreboard = getScoreboard();
        if (sidebarObjective == null || sidebarObjective.getScoreboard() != scoreboard) {
            sidebarObjective = scoreboard.getObjective("sidebar");
            if (sidebarObjective != null) {
                sidebarObjective.setDisplayName(manager.getTitle());
            }
        }
        if (sidebarObjective == null) {
            sidebarObjective = scoreboard.registerNewObjective("sidebar", "dummy", manager.getTitle());
        }
        if (sidebarObjective.getDisplaySlot() != DisplaySlot.SIDEBAR) {
            sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        if (sidebar != null && sidebar.size() > 0) {
            for (int i = 0; i < 15; i++) {
                if (i < sidebar.size()) {
                    if (sidebar.size() > this.sidebar.size() || !sidebar.get(i).equals(this.sidebar.get(i))) {
                        String line = trim(sidebar.get(i), 64);
                        sidebarObjective.getScore(ChatColor.values()[i].toString()).setScore(0);
                        Team team = scoreboard.getTeam("sidebar" + i);
                        if (team == null) {
                            team = scoreboard.registerNewTeam("sidebar" + i);
                            team.addEntry(ChatColor.values()[i].toString());
                        }
                        team.setPrefix(line);
                    }
                } else {
                    scoreboard.resetScores(ChatColor.values()[i].toString());
                }

            }
            this.sidebar = sidebar;
        }
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
