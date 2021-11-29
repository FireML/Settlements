package de.themoep.settlements.scoreboard.commands;

import de.themoep.minedown.MineDown;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.scoreboard.ScoreboardManager;
import de.themoep.settlements.utils.MessageUtils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandScoreboardAdmin extends SubCommand {
    private static final int MAX_WIDTH = 64;

    public CommandScoreboardAdmin(ScoreboardManager manager) {
        super(null, "scoreboardadmin clear|add <line>", "sba", "boardadmin", "sidebaradmin", "sideadmin");
        setPermission("settlements.admin.scoreboard");
        registerCommand(new SubCommand(this, "clear", "reset", "remove") {
            @Override
            public boolean run(CommandSender s, String base, String alias, String... args) {
                manager.addCustomLines(null);
                manager.update();
                MessageUtils.tell(s, getMessage("cleared"));
                return true;
            }
        });
        registerCommand(new SubCommand(this, "add <line>") {
            @Override
            public boolean run(CommandSender s, String base, String alias, String... args) {
                String line = TextComponent.toLegacyText(MineDown.parse(String.join(" ", args)));
                manager.addCustomLines(listByWidth(line));
                manager.update();
                MessageUtils.tell(s, getMessage("added"), "line", line);
                return true;
            }
        });
    }

    @Override
    public boolean run(CommandSender s, String base, String alias, String... args) {
        return false;
    }

    private List<String> listByWidth(String s) {
        List<String> l = new ArrayList<>();
        String progress = "";
        for (String var : s.split(" ")) {
            progress += var;
            if (l.size() == 0) {
                if (var.length() > MAX_WIDTH) {
                    var = var.substring(0, MAX_WIDTH);
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
