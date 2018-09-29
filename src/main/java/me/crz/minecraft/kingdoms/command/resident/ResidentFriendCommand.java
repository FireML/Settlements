package me.crz.minecraft.kingdoms.command.resident;

import java.util.Arrays;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.resident.friend.FriendListCommand;
import me.crz.minecraft.kingdoms.command.resident.friend.FriendRemoveCommand;
import me.crz.minecraft.kingdoms.command.resident.friend.FriendAddCommand;

import java.util.HashMap;
import java.util.Map;

import me.crz.minecraft.kingdoms.command.SubCommand;

public class ResidentFriendCommand extends SubCommand {
    private Map<String, SubCommand> subCommands;
    private StringBuilder sb;

    public ResidentFriendCommand() {
        super("Friend based commands", "friend");
        subCommands = new HashMap<>();
        this.setPermission("village.user");
        (sb = new StringBuilder()).append("&8&l--------&r&c&lVillage Commands&r&8&l--------");
        sb.append("\n&8[&9*&8] &b/{alias} show <name> &7- Shows you information about a resident");
        sb.append("\n&8[&9*&8] &b/{alias} friend add <name> &7- Adds a player into your friends");
        sb.append("\n&8[&9*&8] &b/{alias} friend remove <name> &7- Remove a player from your friends");
        sb.append("\n&8[&9*&8] &b/{alias} friend list &7- Shows the list of friends you have");
        sb.append("\n&8[&9*&8] &b/{alias} help &7- Shows this");
        subCommands.put("add", new FriendAddCommand());
        subCommands.put("remove", new FriendRemoveCommand());
        subCommands.put("list", new FriendListCommand());
    }

    @Override
    public void run(final Player p, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(p, sb.toString(), "{alias}", base + " " + alias);
            return;
        }
        final String aliasUsed = args[0].toLowerCase();
        if (!subCommands.containsKey(aliasUsed)) {
            MessageUtils.tell(p, "&c&lUnknown command!");
            return;
        }
        final SubCommand subCmd = subCommands.get(aliasUsed);
        if (p.hasPermission(subCmd.getPermission())) {
            subCmd.run(p, alias, aliasUsed, Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        MessageUtils.tell(p, MessageUtils.Message.NO_PERMISSION);
    }
}
