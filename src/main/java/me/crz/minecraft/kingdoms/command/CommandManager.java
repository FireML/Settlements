package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.chat.commands.IgnoreCommand;
import me.crz.minecraft.kingdoms.chat.commands.ReplyCommand;
import me.crz.minecraft.kingdoms.essentials.command.Command;
import me.crz.minecraft.kingdoms.chat.commands.MessageCommand;
import org.bukkit.command.CommandExecutor;
import me.crz.minecraft.kingdoms.Base;

public class CommandManager extends Base
{
    public CommandManager() {
        super();
        this.registerCommand((CommandExecutor)new VillageCommand(), "village");
        this.registerCommand((CommandExecutor)new ChatCommand(), "chat");
        this.registerCommand((CommandExecutor)new VillageAdminCommand(), "villageadmin");
        this.registerCommand((CommandExecutor)new ResidentCommand(), "resident");
        me.crz.minecraft.kingdoms.essentials.command.CommandManager.getInstance().registerCommand((Command)new MessageCommand());
        me.crz.minecraft.kingdoms.essentials.command.CommandManager.getInstance().registerCommand((Command)new ReplyCommand());
        me.crz.minecraft.kingdoms.essentials.command.CommandManager.getInstance().registerCommand((Command)new IgnoreCommand());
    }
    
    public void registerCommand(final CommandExecutor ex, final String name) {
        Base.getPlugin().getCommand(name).setExecutor(ex);
    }
}
