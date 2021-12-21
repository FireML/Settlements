package de.themoep.settlements.scoreboard;

import de.themoep.settlements.scoreboard.commands.CommandScoreboard;
import de.themoep.settlements.scoreboard.commands.CommandScoreboardAdmin;
import de.themoep.settlements.scoreboard.listeners.ScoreboardListeners;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.CommandManager;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager implements ConfigMessages {
    private static ScoreboardManager instance;

    private Map<UUID, CScoreBoard> scoreboards = new HashMap<>();
    private List<String> customLines;

    private ScoreBoard scoreboardPolicy = new ScoreBoard(this);
    private String title;
    private long lastUpdate = System.currentTimeMillis();
    private long updateRate;

    private void setup() {
        title = getText("sidebar.title");
        SettlementsPlugin.getInstance().registerListener(new ScoreboardListeners(this));
        updateRate = Config.Sidebar.UPDATE;
        if (updateRate > 0) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(SettlementsPlugin.getInstance(), this::update, 0L, updateRate * 20);
        }
        SettlementsPlugin.getInstance().registerListener(new ScoreboardAnimation(this));
        CommandManager.getInstance().registerCommand(new CommandScoreboard());
        CommandManager.getInstance().registerCommand(new CommandScoreboardAdmin(this), false);

        SettlementsPlugin.logDebug("Loaded ScoreboardManager");
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

    public ScoreBoard getScoreboardPolicy() {
        return scoreboardPolicy;
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

    public long getUpdateRate() {
        return updateRate;
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
        return instance;
    }

    public static void load() {
        if (instance == null) {
            instance = new ScoreboardManager();
            instance.setup();
        }
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
