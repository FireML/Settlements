package me.crz.minecraft.kingdoms.scoreboard;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

public class ScoreboardAnimation implements Listener {
    private final ScoreboardManager scoreboardManager;
    private String title;
    private String PRIMARY;
    private String SECONDARY;
    private String TRANSITION;
    private String PRIMARYT;
    private String SECONDARYT;
    private boolean stage;
    private int counter;
    private String bold;

    public ScoreboardAnimation(final ScoreboardManager scoreboardManager) {
        super();
        bold = String.valueOf(ChatColor.BOLD);
        this.scoreboardManager = scoreboardManager;
        title = scoreboardManager.getTitleStripped();
        PRIMARY = ChatColor.WHITE + bold;
        SECONDARY = ChatColor.AQUA + bold;
        TRANSITION = ChatColor.WHITE + bold;
        PRIMARYT = MessageUtils.Message.SERVER_NAME.getText();
        SECONDARYT = MessageUtils.invertColors(MessageUtils.Message.SERVER_NAME.getText());
        stage = false;
        counter = -1;
        KingdomsPlugin.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(KingdomsPlugin.getInstance(), this::onUpdate, 0L, 2L);
    }

    public void onUpdate() {
        ++counter;
        String s;
        if (counter < 0) {
            if (counter > -48 && counter < -36) {
                if (counter % 4 == 0) {
                    stage = !stage;
                }
                s = (stage ? PRIMARYT : SECONDARYT);
            } else if (counter >= -36) {
                s = PRIMARYT;
            } else {
                s = SECONDARYT;
            }
        } else if (counter == 0) {
            s = PRIMARYT;
        } else if (counter == title.length() + 1) {
            s = SECONDARYT;
            counter = -52;
        } else {
            s = String.valueOf(SECONDARY) + title.substring(0, counter - 1) + TRANSITION + title.substring(counter - 1, counter) + PRIMARY + title.substring(counter, title.length());
        }
        scoreboardManager.setTitle(s);
    }
}
