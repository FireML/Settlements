package me.crz.minecraft.kingdoms.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

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

    public void send() {
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            this.send(pl);
        }
    }

    public void send(Player p) {
        if ((title != null && !title.isEmpty()) || (subtitle != null && !subtitle.isEmpty())) {
            p.sendTitle(title, subtitle, in, stay, out);
        }
        if (actionBar != null && !actionBar.isEmpty()) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBar));
        }
    }
}
