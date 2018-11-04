package me.crz.minecraft.kingdoms.utils;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TitleBuilder {
    private String title;
    private String subtitle;
    private String actionBar;
    private int in = 20;
    private int stay = 60;
    private int out = 20;

    public TitleBuilder() {
        super();
    }

    public TitleBuilder withTimings(int in, int stay, int out) {
        this.in = in;
        this.stay = stay;
        this.out = out;
        return this;
    }

    public TitleBuilder withTitle(String title) {
        this.title = MessageUtils.replaceColorCodes(title);
        return this;
    }

    public TitleBuilder withSubtitle(String subtitle) {
        this.subtitle = MessageUtils.replaceColorCodes(subtitle);
        return this;
    }

    public TitleBuilder withActionbar(String msg) {
        this.actionBar = MessageUtils.replaceColorCodes(msg);
        return this;
    }

    public void send(String... replacements) {
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            this.send(pl, replacements);
        }
    }

    public void send(Player p, String... replacements) {
        if ((title != null && !title.isEmpty()) || (subtitle != null && !subtitle.isEmpty())) {
            p.sendTitle(
                    MessageUtils.replacePlaceholders(title, replacements),
                    MessageUtils.replacePlaceholders(subtitle, replacements),
                    in, stay, out);
        }
        if (actionBar != null && !actionBar.isEmpty()) {
            String replacedActionBar = MessageUtils.replacePlaceholders(actionBar, replacements);
            p.sendActionBar(replacedActionBar);
            Bukkit.getScheduler().runTaskLater(KingdomsPlugin.getInstance(), () -> p.sendActionBar(replacedActionBar), 2 * 20);
        }
    }
}
