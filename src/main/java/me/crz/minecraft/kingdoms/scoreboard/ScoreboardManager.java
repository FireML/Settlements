package me.crz.minecraft.kingdoms.scoreboard;


import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.ScoreBoard;
import me.crz.minecraft.kingdoms.command.CommandManager;
import me.crz.minecraft.kingdoms.scoreboard.commands.CommandRainbowtag;
import me.crz.minecraft.kingdoms.scoreboard.commands.CommandScoreboardAdmin;
import me.crz.minecraft.kingdoms.scoreboard.listeners.ScoreboardListeners;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager implements ConfigMessages {
    private static final ChatColor[] RAINBOW = {
            ChatColor.RED,
            ChatColor.GOLD,
            ChatColor.YELLOW,
            ChatColor.GREEN,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_AQUA,
            ChatColor.BLUE,
            ChatColor.DARK_BLUE,
            ChatColor.DARK_PURPLE,
            ChatColor.LIGHT_PURPLE,
    };
    private static ScoreboardManager instance;

    private Map<UUID, CScoreBoard> scoreboards = new HashMap<>();
    private List<String> customLines;
    private Map<UUID, Integer> rainbowTags = new HashMap<>();

    private ScoreboardPolicy scoreboardPolicy = new ScoreBoard(this);
    private String title;
    private long lastUpdate = System.currentTimeMillis();

    public ScoreboardManager() {
        super();
        title = getText("sidebar.title");
        Base.registerListener(new ScoreboardListeners(this));
        Base.registerListener(new ScoreboardAnimation(this));
        CommandManager.getInstance().registerCommand(new CommandScoreboardAdmin(this), false);
        CommandManager.getInstance().registerCommand(new CommandRainbowtag(this), false);
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

    public Map<UUID, Integer> getRainbowTags() {
        return rainbowTags;
    }

    /**
     * Check whether or not a player has a rainbow tag enabled
     * @param p The player to check
     * @return Whether or not it is enabled
     */
    public boolean hasRainbowTag(Player p) {
        return rainbowTags.get(p.getUniqueId()) != null;
    }

    /**
     * Toggle the rainbow tag
     * @param p The player to toggle the tag for
     * @return <tt>true</tt> if it now enabled; <tt>false</tt> if not
     */
    public boolean toggleRainbowTag(Player p) {
        if (rainbowTags.remove(p.getUniqueId()) == null) {
            rainbowTags.put(p.getUniqueId(), 0);
            return true;
        }
        return false;
    }

    public ChatColor getNextRainbowColor(Player player) {
        if (hasRainbowTag(player)) {
            int nextIndex = rainbowTags.get(player.getUniqueId()) + 1;
            if (nextIndex >= RAINBOW.length) {
                nextIndex = 0;
            }
            rainbowTags.put(player.getUniqueId(), nextIndex);
            return RAINBOW[nextIndex];
        }
        return ChatColor.RESET;
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
