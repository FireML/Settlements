package me.crz.minecraft.kingdoms.scoreboard;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.ScoreBoard;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.scoreboard.commands.CommandScoreboard;
import me.crz.minecraft.kingdoms.scoreboard.commands.CommandScoreboardAdmin;
import me.crz.minecraft.kingdoms.scoreboard.listeners.ScoreboardListeners;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager implements ConfigMessages {
    private static ScoreboardManager instance;

    private Map<UUID, CScoreBoard> scoreboards = new HashMap<>();
    private List<String> customLines;

    private ScoreboardPolicy scoreboardPolicy = new ScoreBoard(this);
    private String title;
    private long lastUpdate = System.currentTimeMillis();

    public ScoreboardManager() {
        super();
        title = getText("sidebar.title");
        Base.registerListener(new ScoreboardListeners(this));
        Base.registerListener(new ScoreboardAnimation(this));
        CommandManager.getInstance().registerCommand(new CommandScoreboard());
        CommandManager.getInstance().registerCommand(new CommandScoreboardAdmin(this), false);
    }

    public void update() {
        if (System.currentTimeMillis() - lastUpdate < 1000L) {
            return;
        }
        lastUpdate = System.currentTimeMillis();
        for (CScoreBoard scoreboard : scoreboards.values()) {
            scoreboard.update();
        }
    }

    public Map<UUID, CScoreBoard> getScoreboards() {
        return scoreboards;
    }

    public ScoreboardPolicy getScoreboardPolicy() {
        return scoreboardPolicy;
    }

    public void setScoreboardPolicy(ScoreboardPolicy scoreboardPolicy) {
        this.scoreboardPolicy = scoreboardPolicy;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleStripped() {
        return ChatColor.stripColor(title);
    }

    public void setTitle(String title) {
        this.title = title;
        for (CScoreBoard scoreboard : scoreboards.values()) {
            scoreboard.setTitle(title);
        }
    }

    public List<String> getCustomLines() {
        return customLines;
    }

    public void addCustomLines(List<String> customLines) {
        if (customLines == null) {
            this.customLines = null;
        } else if (this.customLines == null) {
            this.customLines = customLines;
        } else {
            this.customLines.addAll(customLines);
        }
    }

    public static ScoreboardManager getInstance() {
        if (instance == null) {
            instance = new ScoreboardManager();
        }
        return instance;
    }

    @Override
    public String type() {
        return "";
    }

    @Override
    public String name() {
        return "scoreboard";
    }
}
