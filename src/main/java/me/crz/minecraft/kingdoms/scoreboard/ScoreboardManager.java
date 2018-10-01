package me.crz.minecraft.kingdoms.scoreboard;


import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;

import me.crz.minecraft.kingdoms.scoreboard.commands.CommandRainbowtag;
import me.crz.minecraft.kingdoms.essentials.command.CommandManager;
import me.crz.minecraft.kingdoms.scoreboard.commands.CommandScoreboard;
import me.crz.minecraft.kingdoms.scoreboard.listeners.ScoreboardListeners;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import me.crz.minecraft.kingdoms.Base;

public class ScoreboardManager extends Base {
    private static ScoreboardManager instance;
    private final Map<UUID, CScoreBoard> scoreboards;
    private List<String> customLines;
    private ScoreboardPolicy scoreboardPolicy;
    private String title;
    private long lastUpdate;
    private String bold;

    public ScoreboardManager() {
        super();
        scoreboards = new HashMap<>();
        lastUpdate = System.currentTimeMillis();
        bold = String.valueOf(ChatColor.BOLD);
        title = MessageUtils.Message.SERVER_NAME.getText();
        Base.registerListener(new ScoreboardListeners(this));
        Base.registerListener(new ScoreboardAnimation(this));
        final CommandScoreboard scoreboard = new CommandScoreboard();
        scoreboard.setManager(this);
        CommandManager.getInstance().registerCommand(scoreboard);
        CommandManager.getInstance().registerCommand(new CommandRainbowtag());
    }

    public void update() {
        if (System.currentTimeMillis() - lastUpdate < 1000L) {
            return;
        }
        lastUpdate = System.currentTimeMillis();
        for (final CScoreBoard scoreboard : scoreboards.values()) {
            scoreboard.update();
        }
    }

    public void update(final Player p) {
        if (scoreboards.containsKey(p.getUniqueId())) {
            scoreboards.get(p.getUniqueId()).update();
        }
    }

    @Deprecated
    public void updateUndername() {
        for (final CScoreBoard scoreboard : scoreboards.values()) {
            scoreboard.updateUndername();
        }
    }

    public Map<UUID, CScoreBoard> getScoreboards() {
        return scoreboards;
    }

    public ScoreboardPolicy getScoreboardPolicy() {
        return scoreboardPolicy;
    }

    public void setScoreboardPolicy(final ScoreboardPolicy scoreboardPolicy) {
        this.scoreboardPolicy = scoreboardPolicy;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleStripped() {
        return ChatColor.stripColor(title);
    }

    public void setTitle(final String title) {
        this.title = title;
        for (final CScoreBoard scoreboard : scoreboards.values()) {
            scoreboard.setTitle(title);
        }
    }

    public List<String> getCustomLines() {
        return customLines;
    }

    public void addCustomLines(final List<String> customLines) {
        if (customLines == null) {
            this.customLines = null;
        } else if (this.customLines == null) {
            this.customLines = customLines;
        } else {
            this.customLines.addAll(customLines);
        }
    }

    public String trimPrefix(final String prefix) {
        if (prefix == null) {
            return null;
        }
        if (prefix.length() < 16) {
            return prefix;
        }
        if (prefix.split(" ").length <= 1) {
            return prefix.substring(0, 16);
        }
        String firstPart = prefix.split(" ")[0];
        final String secondPart = prefix.substring((String.valueOf(firstPart) + " ").length());
        firstPart = firstPart.substring(0, 16 - (String.valueOf(secondPart) + " ").length());
        return String.valueOf(firstPart) + " " + secondPart;
    }

    public static ScoreboardManager getInstance() {
        if (instance == null) {
            instance = new ScoreboardManager();
        }
        return instance;
    }
}
