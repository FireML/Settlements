package me.crz.minecraft.kingdoms.scoreboard.commands;

import java.util.ArrayList;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.ChatColor;

import java.util.List;

import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.scoreboard.ScoreboardManager;

public class CommandScoreboard extends SubCommand {
    private static final int MAX_WIDTH = 40;

    public CommandScoreboard(ScoreboardManager manager) {
        super(null, "scoreboard clear|add <line>", "sb", "board", "sidebar", "side");
        setPermission("kingdoms.scoreboard");
        registerCommand(new SubCommand(this, "clear", "reset", "remove") {
            @Override
            public boolean run(Player p, String base, String alias, String... args) {
                manager.addCustomLines(null);
                manager.update();
                MessageUtils.tell(p, getMessage("cleared"));
                return true;
            }
        });
        registerCommand(new SubCommand(this, "add <line>") {
            @Override
            public boolean run(Player p, String base, String alias, String... args) {
                String line = ChatColor.translateAlternateColorCodes('&', String.join(" ", args));
                manager.addCustomLines(listByWidth(line));
                manager.update();
                MessageUtils.tell(p, getMessage("added"), "line", line);
                return true;
            }
        });
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        return false;
    }

    private List<String> listByWidth(String s) {
        List<String> l = new ArrayList<>();
        String progress = "";
        for (String var : s.split(" ")) {
            progress += var;
            if (l.size() == 0) {
                if (var.length() > MAX_WIDTH) {
                    var = var.substring(0, 40);
                }
                l.add(var);
            } else if ((l.get(l.size() - 1) + var).length() > MAX_WIDTH) {
                l.add(ChatColor.getLastColors(progress) + ChatColor.stripColor((var.length() > MAX_WIDTH) ? var.substring(0, MAX_WIDTH) : var));
            } else {
                l.set(l.size() - 1, l.get(l.size() - 1) + " " + var);
            }
        }
        for (int i = 0; i < l.size(); ++i) {
            l.set(i, l.get(i).trim());
        }
        return l;
    }
}
