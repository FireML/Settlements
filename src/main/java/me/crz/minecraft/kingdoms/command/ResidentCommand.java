package me.crz.minecraft.kingdoms.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import me.crz.minecraft.kingdoms.utils.MessageUtils;

import java.util.Arrays;

import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.resident.ResidentFriendCommand;
import me.crz.minecraft.kingdoms.command.resident.ResidentHelpCommand;
import me.crz.minecraft.kingdoms.command.resident.ResidentShowCommand;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandExecutor;

public class ResidentCommand implements CommandExecutor {
    private Map<String, SubCommand> subCommands;

    public ResidentCommand() {
        super();
        (subCommands = new HashMap<>()).put("show", new ResidentShowCommand());
        subCommands.put("help", new ResidentHelpCommand());
        subCommands.put("friend", new ResidentFriendCommand());
    }

    public void run(Player p, String alias, String[] args) {
        if (args.length < 1) {
            subCommands.get("help").run(p, alias, "help");
            return;
        }
        String aliasUsed = args[0].toLowerCase();
        if (!subCommands.containsKey(aliasUsed)) {
            subCommands.get("show").run(p, alias, "show", args);
            return;
        }
        SubCommand subCmd = subCommands.get(aliasUsed);
        if (p.hasPermission(subCmd.getPermission())) {
            subCmd.run(p, alias, aliasUsed, Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        MessageUtils.tell(p, MessageUtils.Message.NO_PERMISSION);
    }

    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            this.run((Player) sender, alias, args);
            return true;
        }
        return false;
    }
}
