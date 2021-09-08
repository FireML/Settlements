package de.themoep.settlements.scoreboard;

import co.aikar.timings.lib.MCTiming;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

public class ScoreboardAnimation implements Listener {
    private final ScoreboardManager manager;
    private String title;
    private String PRIMARY;
    private String SECONDARY;
    private String PRIMARYT;
    private String SECONDARYT;
    private boolean stage;
    private int counter;
    private String bold;

    public ScoreboardAnimation(ScoreboardManager manager) {
        bold = String.valueOf(ChatColor.BOLD);
        this.manager = manager;
        PRIMARYT = manager.getText("sidebar.title");
        SECONDARYT = MessageUtils.invertColors(manager.getText("sidebar.title"));
        title = manager.getTitleStripped();
        PRIMARY = ChatColor.getLastColors(PRIMARYT);
        SECONDARY = ChatColor.getLastColors(SECONDARYT);
        stage = false;
        counter = -1;
        long speed = Config.Sidebar.HEADER_SPEED;
        if (speed > 0) {
            SettlementsPlugin.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(SettlementsPlugin.getInstance(), this::onUpdate, speed, speed);
        }
    }

    public void onUpdate() {
        MCTiming timing = SettlementsPlugin.getInstance().getAndStartTiming("Scoreboard Title Animation");
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
            s = SECONDARY + title.substring(0, counter) + PRIMARY + title.substring(counter);
        }
        manager.setTitle(s);
        timing.stopTiming();
    }
}
