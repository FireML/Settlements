package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.utils.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;

import me.crz.minecraft.kingdoms.command.admin.VillageAdminSocialSpyCommand;
import me.crz.minecraft.kingdoms.command.admin.VillageEmgcyCommand;
import me.crz.minecraft.kingdoms.command.admin.VillageAdminJoinCommand;
import me.crz.minecraft.kingdoms.command.admin.VillageAdminRankCommand;
import me.crz.minecraft.kingdoms.command.admin.VillageAdminWorldCommand;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandExecutor;

public class VillageAdminCommand implements CommandExecutor {
    private Map<String, SubCommand> subCommands;
    private StringBuilder help;

    public VillageAdminCommand() {
        super();
        subCommands = new HashMap<>();
        this.registerCommand(new VillageAdminWorldCommand());
        this.registerCommand(new VillageAdminRankCommand());
        this.registerCommand(new VillageAdminJoinCommand());
        this.registerCommand(new VillageEmgcyCommand());
        this.registerCommand(new VillageAdminSocialSpyCommand());
        help = new StringBuilder("&8&l--------&r&c&lVillage Admin Commands&r&8&l--------");
        for (final SubCommand subCmd : subCommands.values()) {
            help.append("\n&c/{base} ");
            help.append(subCmd.getBaseAlias());
            help.append(": ");
            help.append(subCmd.getDescription());
        }
    }

    public void registerCommand(final SubCommand c) {
        for (final String alias : c.getAliases()) {
            subCommands.put(alias.toLowerCase(), c);
        }
    }

    public void run(final Player p, final String alias, final String[] args) {
        if (args.length < 1) {
            MessageUtils.tell(p, help.toString(), "{base}", alias);
            return;
        }
        final String aliasUsed = args[0].toLowerCase();
        if (!subCommands.containsKey(aliasUsed)) {
            MessageUtils.tell(p, "&c&lUnknown command!");
            return;
        }
        if (!p.hasPermission(subCommands.get(aliasUsed).getPermission())) {
            MessageUtils.tell(p, Message.get("no-permission"));
            return;
        }
        subCommands.get(aliasUsed).run(p, alias, aliasUsed, (String[]) Arrays.copyOfRange(args, 1, args.length));
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (sender instanceof Player) {
            this.run((Player) sender, alias, args);
            return true;
        }
        return false;
    }
}
