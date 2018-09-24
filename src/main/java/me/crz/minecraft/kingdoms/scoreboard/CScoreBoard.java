package me.crz.minecraft.kingdoms.scoreboard;

import org.bukkit.ChatColor;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import java.util.Collection;
import org.bukkit.scoreboard.Team;
import java.util.Iterator;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import java.util.HashMap;
import java.util.List;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.Map;

public class CScoreBoard
{
    private static final Map<String, UUID> staticNametags;
    private Player p;
    private Scoreboard scoreboard;
    private Objective sidebarObjective;
    private Objective undernameObjective;
    private Objective tablistObjective;
    private List<String> sidebar;
    
    static {
        staticNametags = new HashMap<String, UUID>();
    }
    
    public CScoreBoard(final Player p) {
        super();
        p = p;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        sidebar = new ArrayList<String>();
        for (final UUID staticNametag : staticNametags.values()) {
            this.setStaticNametag0(Bukkit.getPlayer(staticNametag));
        }
        this.getPlayer().setScoreboard(scoreboard);
    }
    
    public void update() {
        this.updateSidebar();
        this.updateTeams();
        this.updateUndername();
        this.updateTablist();
    }
    
    public void updateSidebar() {
        if (ScoreboardManager.getInstance().getCustomLines() == null && ScoreboardManager.getInstance().getScoreboardPolicy().getSidebar(p) == null) {
            if (sidebarObjective != null) {
                sidebarObjective.unregister();
                sidebarObjective = null;
            }
            return;
        }
        this.setSidebar((ScoreboardManager.getInstance().getCustomLines() == null) ? ScoreboardManager.getInstance().getScoreboardPolicy().getSidebar(p) : ScoreboardManager.getInstance().getCustomLines());
    }
    
    public void updateTeams() {
        for (final Team team : new ArrayList<Team>(scoreboard.getTeams())) {
            if (staticNametags.containsKey(team.getName())) {
                continue;
            }
            final Player pl = Bukkit.getPlayer(team.getName().split("-")[1]);
            if (pl == null) {
                team.unregister();
            }
            else {
                final String prefix = ScoreboardManager.getInstance().getScoreboardPolicy().getPrefix(p, pl);
                final String suffix = ScoreboardManager.getInstance().getScoreboardPolicy().getSuffix(p, pl);
                if (prefix != null && !team.getPrefix().equals(prefix)) {
                    team.setPrefix(ScoreboardManager.getInstance().trimPrefix(prefix));
                }
                else if (prefix == null && team.getPrefix() != null) {
                    team.setPrefix("");
                }
                if (suffix != null && !team.getSuffix().equals(suffix)) {
                    team.setSuffix(ScoreboardManager.getInstance().trimPrefix(suffix));
                }
                else {
                    if (suffix != null || team.getSuffix() == null) {
                        continue;
                    }
                    team.setSuffix("");
                }
            }
        }
        for (final Player pl2 : Bukkit.getOnlinePlayers()) {
            final String teamName = getTeamName(pl2);
            if (scoreboard.getTeam(teamName) == null) {
                final Team team2 = scoreboard.registerNewTeam(teamName);
                final String prefix2 = ScoreboardManager.getInstance().getScoreboardPolicy().getPrefix(p, pl2);
                final String suffix2 = ScoreboardManager.getInstance().getScoreboardPolicy().getSuffix(p, pl2);
                if (prefix2 != null) {
                    team2.setPrefix(ScoreboardManager.getInstance().trimPrefix(prefix2));
                }
                if (suffix2 != null) {
                    team2.setSuffix(ScoreboardManager.getInstance().trimPrefix(suffix2));
                }
                team2.addEntry(pl2.getName());
            }
        }
    }
    
    public void updateUndername() {
        if (ScoreboardManager.getInstance().getScoreboardPolicy().getUndername(p) == null) {
            if (undernameObjective != null) {
                undernameObjective.unregister();
                undernameObjective = null;
            }
            return;
        }
        if (undernameObjective == null) {
            (undernameObjective = scoreboard.registerNewObjective("undername", "dummy")).setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
        if (!undernameObjective.getDisplayName().equals(ScoreboardManager.getInstance().getScoreboardPolicy().getUndername(p))) {
            undernameObjective.setDisplayName(ScoreboardManager.getInstance().getScoreboardPolicy().getUndername(p));
        }
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            final String name = pl.getName();
            final int score = ScoreboardManager.getInstance().getScoreboardPolicy().getUndernameScore(p, pl);
            if (undernameObjective.getScore(name).getScore() != score) {
                undernameObjective.getScore(name).setScore(score);
            }
        }
    }
    
    public void updateTablist() {
        if (ScoreboardManager.getInstance().getScoreboardPolicy().getTablist(p) == null) {
            if (tablistObjective != null) {
                tablistObjective.unregister();
                tablistObjective = null;
            }
            return;
        }
        if (tablistObjective == null) {
            (tablistObjective = scoreboard.registerNewObjective("tablist", "dummy")).setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }
        if (!tablistObjective.getDisplayName().equals(ScoreboardManager.getInstance().getScoreboardPolicy().getTablist(p))) {
            tablistObjective.setDisplayName(ScoreboardManager.getInstance().getScoreboardPolicy().getTablist(p));
        }
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            final String name = pl.getName();
            final int score = ScoreboardManager.getInstance().getScoreboardPolicy().getTablistScore(p, pl);
            if (tablistObjective.getScore(name).getScore() != score) {
                tablistObjective.getScore(name).setScore(score);
            }
        }
    }
    
    public void setSidebar(final List<String> sidebar) {
        if (sidebarObjective == null) {
            (sidebarObjective = scoreboard.registerNewObjective("title", "dummy")).setDisplayName(ScoreboardManager.getInstance().getTitle());
            sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        final List<String> set = new ArrayList<String>();
        if (sidebar != null && sidebar.size() > 0) {
            int score = 15;
            for (final String line : sidebar) {
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
        for (final String last : this.sidebar) {
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
    
    public void setTitle(final String title) {
        if (sidebarObjective != null) {
            sidebarObjective.setDisplayName(title);
        }
    }
    
    public static void setStaticNametag(final Player player) {
        if (!staticNametags.containsKey(getTeamName(player))) {
            staticNametags.put(getTeamName(player), player.getUniqueId());
        }
        for (final CScoreBoard scoreboard : ScoreboardManager.getInstance().getScoreboards().values()) {
            scoreboard.setStaticNametag0(player);
        }
    }
    
    private void setStaticNametag0(final Player p) {
        if (p == null) {
            return;
        }
        Team team = scoreboard.getTeam(getTeamName(p));
        if (team != null) {
            team.unregister();
        }
        team = scoreboard.registerNewTeam(getTeamName(p));
        final String prefix = ScoreboardManager.getInstance().getScoreboardPolicy().getPrefix(null, p);
        final String suffix = ScoreboardManager.getInstance().getScoreboardPolicy().getSuffix(null, p);
        if (prefix != null) {
            team.setPrefix(prefix);
        }
        if (suffix != null) {
            team.setSuffix(suffix);
        }
        team.addEntry(p.getName());
        if (undernameObjective == null) {
            (undernameObjective = scoreboard.registerNewObjective("undername", "dummy")).setDisplaySlot(DisplaySlot.BELOW_NAME);
            undernameObjective.setDisplayName(ScoreboardManager.getInstance().getScoreboardPolicy().getUndername(p));
        }
        undernameObjective.getScore(p.getName()).setScore(ScoreboardManager.getInstance().getScoreboardPolicy().getUndernameScore((Player)null, p));
    }
    
    public static String getTeamName(final Player subject) {
        final displayRank disp = displayRank.get(ChatColor.stripColor(MessageUtils.replaceColorCodes(KingdomsPlugin.chat.getPlayerPrefix(subject))).toLowerCase().replace((CharSequence)"+", (CharSequence)"P"));
        String sort;
        for (sort = String.valueOf(displayRank.values().length - disp.ordinal()); sort.length() < String.valueOf(displayRank.values().length).length(); sort = "0" + sort) {}
        String displayName = String.valueOf(sort) + "-" + subject.getName();
        displayName = displayName.substring(0, Math.min(displayName.length(), 16));
        return displayName;
    }
    
    public static Map<String, UUID> getStaticNametags() {
        return staticNametags;
    }
    
    public Player getPlayer() {
        return p;
    }
    
    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}
