package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.chat.commands.IgnoreCommand;
import me.crz.minecraft.kingdoms.command.resident.ResidentClaimTransferCommand;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.resident.ResidentFriendCommand;
import me.crz.minecraft.kingdoms.command.resident.ResidentShowCommand;

public class ResidentCommand extends SubCommand {

    public ResidentCommand() {
        super(null, "resident");
        registerCommand(new ResidentShowCommand(this));
        ResidentFriendCommand friendCommand = new ResidentFriendCommand(this);
        registerCommand(friendCommand);
        CommandManager.getInstance().registerCommand(friendCommand);
        registerCommand(new IgnoreCommand());
        registerCommand(new ResidentClaimTransferCommand(this));
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        return false;
    }
}
