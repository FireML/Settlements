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
        this.title = title;
        return this;
    }

    public TitleBuilder withSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public TitleBuilder withActionbar(String msg) {
        this.actionBar = msg;
        return this;
    }

    public void send() {
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            this.send(pl);
        }
    }

    public void send(Player p) {
        if (title != null || subtitle != null) {
            p.sendTitle(title, subtitle, in, stay, out);
        }
        if (actionBar != null) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionBar));
        }
    }
}
