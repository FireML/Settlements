package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.chat.commands.IgnoreCommand;
import me.crz.minecraft.kingdoms.chat.commands.ReplyCommand;
import me.crz.minecraft.kingdoms.chat.commands.MessageCommand;
import org.bukkit.command.CommandExecutor;
import me.crz.minecraft.kingdoms.Base;

public class CommandManager extends Base {
    public CommandManager() {
        super();
        this.registerCommand(new VillageCommand(), "village");
        this.registerCommand(new ChatCommand(), "chat");
        this.registerCommand(new VillageAdminCommand(), "villageadmin");
        this.registerCommand(new ResidentCommand(), "resident");
        me.crz.minecraft.kingdoms.essentials.command.CommandManager.getInstance().registerCommand(new MessageCommand());
        me.crz.minecraft.kingdoms.essentials.command.CommandManager.getInstance().registerCommand(new ReplyCommand());
        me.crz.minecraft.kingdoms.essentials.command.CommandManager.getInstance().registerCommand(new IgnoreCommand());
    }

    public void registerCommand(final CommandExecutor ex, final String name) {
        Base.getPlugin().getCommand(name).setExecutor(ex);
    }
}
