package me.crz.minecraft.kingdoms.scoreboard;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class TabHeader {
    private String header;
    private String footer;

    public TabHeader(final String header, final String footer) {
        super();
        this.header = MessageUtils.replaceColorCodes(header);
        this.footer = MessageUtils.replaceColorCodes(footer);
    }

    public void send() {
        this.send(new ArrayList<>(Bukkit.getOnlinePlayers()));
    }

    public void setFooter(final String footer) {
        this.footer = footer;
    }

    public void send(final Collection<Player> players) {
        for (final Player pl : players) {
            this.send(pl);
        }
    }

    public void send(final Player... players) {
        for (final Player pl : players) {
            this.send(pl);
        }
    }

    public void send(final Player p) {
        p.setPlayerListHeaderFooter(header, footer);
    }
}
