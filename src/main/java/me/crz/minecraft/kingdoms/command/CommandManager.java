package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.chat.commands.IgnoreCommand;
import me.crz.minecraft.kingdoms.chat.commands.MessageCommand;
import me.crz.minecraft.kingdoms.chat.commands.ReplyCommand;
import me.crz.minecraft.kingdoms.command.admin.VillageAdminCommand;
import me.crz.minecraft.kingdoms.command.empire.EmpireCommand;
import me.crz.minecraft.kingdoms.command.friend.FriendCommand;
import me.crz.minecraft.kingdoms.command.plot.PlotCommand;
import me.crz.minecraft.kingdoms.command.resident.ResidentCommand;
import me.crz.minecraft.kingdoms.command.village.VillageCommand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class CommandManager extends Base implements CommandExecutor {
    private static CommandManager instance;
    private Map<String, SubCommand> commandMap = new LinkedHashMap<>();

    private CommandManager() {
        super();
    }

    private void registerCommands() {
        registerCommand(new PlotCommand());
        registerCommand(new VillageCommand());
        registerCommand(new EmpireCommand());
        registerCommand(new ChatCommand());
        registerCommand(new VillageAdminCommand());
        registerCommand(new ResidentCommand());
        registerCommand(new FriendCommand());
        registerCommand(new MessageCommand());
        registerCommand(new ReplyCommand());
        registerCommand(new IgnoreCommand());
    }

    public void registerCommand(SubCommand ex) {
        commandMap.put(ex.name().toLowerCase(), ex);
        PluginCommand command = getPlugin().getCommand(ex.name());
        if (command != null) {
            command.setExecutor(this);
        } else {
            getPlugin().getLogger().log(Level.SEVERE, "Tried to register command by the name of " + ex.name() + " but it's not defined in the plugin.yml?");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            SubCommand cmd = commandMap.get(command.getName().toLowerCase());
            if (cmd == null) {
                MessageUtils.tell((Player) sender, "&c&lUnknown command!");
            } else if (!cmd.execute((Player) sender, null, alias, args)) {
                if (cmd.getMessage("usage").exists()) {
                    MessageUtils.tell((Player) sender, cmd.getMessage("usage"), "alias", alias);
                } else if (!cmd.sendUsageMessage((Player) sender, null, alias)) {
                    MessageUtils.tell((Player) sender, "&c" + cmd.getUsage(null));
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
            instance.registerCommands();
        }
        return instance;
    }
}
