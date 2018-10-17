package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.chat.commands.IgnoreCommand;
import me.crz.minecraft.kingdoms.chat.commands.MessageCommand;
import me.crz.minecraft.kingdoms.chat.commands.ReplyCommand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommandManager extends Base implements CommandExecutor {
    private static CommandManager instance;
    private Map<String, SubCommand> commandMap = new LinkedHashMap<>();

    public CommandManager() {
        super();
        registerCommand(new VillageCommand());
        registerCommand(new ChatCommand());
        registerCommand(new VillageAdminCommand());
        registerCommand(new ResidentCommand());
        registerCommand(new MessageCommand());
        registerCommand(new ReplyCommand());
        registerCommand(new IgnoreCommand());
    }

    public void registerCommand(SubCommand ex) {
        commandMap.put(ex.name().toLowerCase(), ex);
        Base.getPlugin().getCommand(ex.name()).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            SubCommand cmd = commandMap.get(command.getName().toLowerCase());
            if (cmd == null) {
                MessageUtils.tell((Player) sender, "&c&lUnknown command!");
            } else if (!cmd.execute((Player) sender, "", alias, args)) {
                if (cmd.getMessage("usage").exists()) {
                    MessageUtils.tell((Player) sender, cmd.getMessage("usage"), "alias", alias);
                } else if (cmd.hasUsage()) {
                    MessageUtils.tell((Player) sender, "&c" + cmd.getUsage(alias));
                } else {
                    return false;
                }
            }
        } else {
            sender.sendMessage(Message.get("error.only-player").getText());
        }
        return true;
    }

    public static CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }
}
