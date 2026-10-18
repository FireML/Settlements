package de.themoep.settlements.commandold;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.commandold.friend.FriendCommand;
import de.themoep.settlements.commandold.plot.PlotCommand;
import de.themoep.settlements.commandold.resident.ResidentCommand;
import de.themoep.settlements.commandold.settlement.SettlementCommand;
import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.permissions.Permission;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements TabExecutor {
    private static CommandManager instance;
    private final Map<String, SubCommand> commandMap = new LinkedHashMap<>();

    private void setup() {
        SettlementsPlugin.logDebug("Loaded CommandManager for " + commandMap.size() + " commands");
    }

    private void registerCommands() {
        registerCommand(new PlotCommand());
        registerCommand(new SettlementCommand());
        registerCommand(new ResidentCommand());
        registerCommand(new FriendCommand());
    }

    public void registerCommand(SubCommand ex) {
        registerCommand(ex, true);
    }

    public void registerCommand(SubCommand ex, boolean isUserCommand) {
        ex.registerPermission();
        if (isUserCommand) {
            Permission commandPerm = Bukkit.getPluginManager().getPermission(ex.getPermission());
            if (commandPerm == null) {
                commandPerm = new Permission(ex.getPermission(), "Allows access to the command /" + ex.getPath());
                Bukkit.getPluginManager().addPermission(commandPerm);
            }
            commandPerm.addParent("settlements.user", true);
        }
        commandMap.put(ex.name().toLowerCase(), ex);
        for (String alias : ex.getAliases()) {
            commandMap.put(alias, ex);
        }
        PluginCommand command = SettlementsPlugin.getInstance().getCommand(ex.name());
        if (command != null) {
            command.setExecutor(this);
            command.setPermission(ex.getPermission());
        } else {
            SettlementsPlugin.getInstance().getLogging().error("Tried to register command by the name of " + ex.name() + " but it's not defined in the plugin.yml?");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        SubCommand cmd = getCommand(command.getName());
        if (cmd == null) {
            MessageUtils.tell(sender, ComponentUtils.parseComponent("&c&lUnknown command!"));
        } else if (!cmd.execute(sender, null, alias, args)) {
            if (cmd.getMessage("usage").exists()) {
                cmd.getMessage("usage").tell(sender, "alias", alias);
            } else if (!cmd.getHelpCommand().run(sender, cmd.getShortestAlias(), "help")) {
                MessageUtils.tell(sender, ComponentUtils.parseComponent("&c" + cmd.getUsage(null)));
            } else {
                return false;
            }
        }
        return true;
    }

    public SubCommand getCommand(String name) {
        return commandMap.get(name.toLowerCase());
    }

    synchronized public static CommandManager getInstance() {
        return instance;
    }

    public static void load() {
        if (instance == null) {
            instance = new CommandManager();
            instance.setup();
            instance.registerCommands();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        SubCommand cmd = getCommand(command.getName());
        if (cmd != null) {
            return cmd.onTabComplete(sender, alias, args);
        }
        return null;
    }
}
