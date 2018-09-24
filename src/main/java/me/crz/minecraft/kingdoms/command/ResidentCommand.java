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

public class ResidentCommand implements CommandExecutor
{
    private Map<String, SubCommand> subCommands;
    
    public ResidentCommand() {
        super();
        (subCommands = new HashMap<String, SubCommand>()).put("show", new ResidentShowCommand());
        subCommands.put("help", new ResidentHelpCommand());
        subCommands.put("friend", new ResidentFriendCommand());
    }
    
    public void run(final Player p, final String alias, final String[] args) {
        final String aliasUsed = args[0].toLowerCase();
        if (!subCommands.containsKey(aliasUsed)) {
            ((SubCommand)subCommands.get("show")).run(p, alias, "show", args);
            return;
        }
        final SubCommand subCmd = (SubCommand)subCommands.get(aliasUsed);
        if (p.hasPermission(subCmd.getPermission())) {
            subCmd.run(p, alias, aliasUsed, (String[])Arrays.<String>asList(args).subList(1, args.length).<String>toArray(new String[0]));
            return;
        }
        MessageUtils.tell(p, MessageUtils.NO_PERMISSION_MESSAGE);
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (sender instanceof Player) {
            this.run((Player)sender, alias, args);
            return true;
        }
        return false;
    }
}
