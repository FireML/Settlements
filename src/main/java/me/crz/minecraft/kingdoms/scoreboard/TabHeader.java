package me.crz.minecraft.kingdoms.scoreboard;

import me.crz.minecraft.kingdoms.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class TabHeader {
    private Message header;
    private Message footer;

    public TabHeader(Message header, Message footer) {
        super();
        this.header = header;
        this.footer = footer;
    }

    public void send(String... replacements) {
        this.send(Bukkit.getOnlinePlayers(), replacements);
    }

    public void send(Collection<? extends Player> players, String... replacements) {
        for (Player pl : players) {
            this.send(pl, replacements);
        }
    }

    public void send(Player[] players, String... replacements) {
        for (Player pl : players) {
            send(pl, replacements);
        }
    }

    public void send(Player p, String... replacements) {
        p.setPlayerListHeaderFooter(header.getReplacedText(replacements), footer.getReplacedText(replacements));
    }
}
