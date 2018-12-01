package me.crz.minecraft.kingdoms.scoreboard;

import co.aikar.timings.Timing;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.ChatColor;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.scoreboard.DisplaySlot;

import org.bukkit.scoreboard.Team;

import java.util.ArrayList;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;

import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.Map;

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
        getPlayer().setScoreboard(scoreboard);
    }

    public void update() {
        try (Timing ignored = KingdomsPlugin.getInstance().getAndStartTiming("Scoreboard Update Sidebar")) {
            updateSidebar();
        }
        if (manager.getUpdateRate() > 0) {
            try (Timing ignored = KingdomsPlugin.getInstance().getAndStartTiming("Scoreboard Update Teams")) {
                updateTeams();
            }
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

    public void updateTeams() {
        for (Team team : new ArrayList<>(scoreboard.getTeams())) {
            Player pl = Bukkit.getPlayer(team.getName().split("-")[1]);
            if (pl == null) {
                team.unregister();
            } else {
                String prefix = manager.getScoreboardPolicy().getPrefix(p, pl);
                String suffix = manager.getScoreboardPolicy().getSuffix(p, pl);
                if (prefix != null && !team.getPrefix().equals(prefix)) {
                    team.setPrefix(trim(prefix, 16));
                } else if (prefix == null && team.getPrefix() != null) {
                    team.setPrefix("");
                }
                if (suffix != null && !team.getSuffix().equals(suffix)) {
                    team.setSuffix(trim(suffix, 16));
                } else {
                    if (suffix != null || team.getSuffix() == null) {
                        continue;
                    }
                    team.setSuffix("");
                }
            }
        }
        for (Player pl2 : Bukkit.getOnlinePlayers()) {
            String teamName = getTeamName(pl2);
            if (scoreboard.getTeam(teamName) == null) {
                Team team2 = scoreboard.registerNewTeam(teamName);
                String prefix2 = manager.getScoreboardPolicy().getPrefix(p, pl2);
                String suffix2 = manager.getScoreboardPolicy().getSuffix(p, pl2);
                if (prefix2 != null) {
                    team2.setPrefix(trim(prefix2, 16));
                }
                if (suffix2 != null) {
                    team2.setSuffix(trim(suffix2, 16));
                }
                team2.addEntry(pl2.getName());
            }
        }
    }

    public void setSidebar(List<String> sidebar) {
        if (sidebarObjective == null) {
            (sidebarObjective = scoreboard.registerNewObjective("title", "dummy", "title")).setDisplayName(manager.getTitle());
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

    private void setStaticNametag0(Player p) {
        if (p == null) {
            return;
        }
        Team team = scoreboard.getTeam(getTeamName(p));
        if (team != null) {
            team.unregister();
        }
        team = scoreboard.registerNewTeam(getTeamName(p));
        String prefix = manager.getScoreboardPolicy().getPrefix(null, p);
        String suffix = manager.getScoreboardPolicy().getSuffix(null, p);
        if (prefix != null) {
            team.setPrefix(prefix);
        }
        if (suffix != null) {
            team.setSuffix(suffix);
        }
        team.addEntry(p.getName());
    }

    public static String getTeamName(Player subject) {
        DisplayRank disp = DisplayRank.get(ChatColor.stripColor(MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(subject))).toLowerCase().replace("+", "P"));
        String sort;
        for (sort = String.valueOf(DisplayRank.values().length - disp.ordinal()); sort.length() < String.valueOf(DisplayRank.values().length).length(); sort = "0" + sort) {
        }
        String displayName = String.valueOf(sort) + "-" + subject.getName();
        displayName = Utils.trim(displayName, 16);
        return displayName;
    }

    public Player getPlayer() {
        return p;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}
