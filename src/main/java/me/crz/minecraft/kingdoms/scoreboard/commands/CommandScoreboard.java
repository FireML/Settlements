package me.crz.minecraft.kingdoms.scoreboard.commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;

import java.util.List;

import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;
import me.crz.minecraft.kingdoms.essentials.command.Command;

public class CommandScoreboard extends Command {
    private final int MAX_WIDTH = 40;
    private ScoreboardManager manager;

    public CommandScoreboard() {
        super("Set the scoreboard for all players", "scoreboard", "sb", "board", "sidebar", "side");
    }

    public void setManager(final ScoreboardManager manager) {
        this.manager = manager;
    }

    public void run(final Player p, final String aliasUsed, final String[] args) {
        if (!p.hasPermission("kingdoms.scoreboard")) {
            p.sendMessage("Sorry, you can only do this on an Event Server!");
            return;
        }
        if (args.length < 1) {
            return;
        }
        if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("remove")) {
            manager.addCustomLines(null);
            manager.update();
            p.sendMessage("Reset scoreboard for all players.");
            return;
        }
        String s = "";
        for (final String arg : args) {
            s = String.valueOf(s) + arg + " ";
        }
        s = ChatColor.translateAlternateColorCodes('&', s.trim());
        manager.addCustomLines(this.listByWidth(s));
        manager.update();
        p.sendMessage(ChatColor.GRAY + "Added '" + ChatColor.WHITE + s + ChatColor.GRAY + "' to the scoreboard.");
    }

    private List<String> listByWidth(final String s) {
        final List<String> l = new ArrayList<>();
        String progress = "";
        String[] split;
        for (int length = (split = s.split(" ")).length, j = 0; j < length; ++j) {
            String var = split[j];
            progress = String.valueOf(progress) + var;
            if (l.size() == 0) {
                if (var.length() > MAX_WIDTH) {
                    var = var.substring(0, 40);
                }
                l.add(var);
            } else if ((String.valueOf(l.get(l.size() - 1)) + var).length() > MAX_WIDTH) {
                l.add(String.valueOf(ChatColor.getLastColors(progress)) + ChatColor.stripColor((var.length() > MAX_WIDTH) ? var.substring(0, MAX_WIDTH) : var));
            } else {
                l.set(l.size() - 1, String.valueOf(l.get(l.size() - 1)) + " " + var);
            }
        }
        for (int i = 0; i < l.size(); ++i) {
            l.set(i, l.get(i).trim());
        }
        return l;
    }
}
