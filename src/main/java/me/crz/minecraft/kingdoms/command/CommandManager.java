package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.commands.IgnoreCommand;
import me.crz.minecraft.kingdoms.chat.commands.ReplyCommand;
import me.crz.minecraft.kingdoms.chat.commands.MessageCommand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import me.crz.minecraft.kingdoms.Base;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandManager extends Base implements CommandExecutor {

    private Map<String, SubCommand> commandMap = new LinkedHashMap<>();

    public CommandManager() {
        super();
        this.registerCommand(new VillageCommand());
        this.registerCommand(new ChatCommand());
        this.registerCommand(new VillageAdminCommand());
        this.registerCommand(new ResidentCommand());
        ConfigManager.save(new File(KingdomsPlugin.getInstance().getDataFolder(), "messages.yml"), ConfigManager.getInstance().getMessagesConfig());
        me.crz.minecraft.kingdoms.essentials.command.CommandManager.getInstance().registerCommand(new MessageCommand());
        me.crz.minecraft.kingdoms.essentials.command.CommandManager.getInstance().registerCommand(new ReplyCommand());
        me.crz.minecraft.kingdoms.essentials.command.CommandManager.getInstance().registerCommand(new IgnoreCommand());
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
                if (Message.get("commands." + cmd.name() + ".usage").exists()) {
                    MessageUtils.tell((Player) sender, Message.get("commands." + cmd.name() + ".usage"), "alias", alias);
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
}
