package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.chat.commands.IgnoreCommand;
import me.crz.minecraft.kingdoms.command.resident.ResidentClaimTransferCommand;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.resident.ResidentFriendCommand;
import me.crz.minecraft.kingdoms.command.resident.ResidentShowCommand;

import java.util.Arrays;

public class ResidentCommand extends SubCommand {

    private final SubCommand showCommand;

    public ResidentCommand() {
        super(null, "resident <name>");
        showCommand = new ResidentShowCommand(this);
        registerCommand(showCommand);
        ResidentFriendCommand friendCommand = new ResidentFriendCommand(this);
        registerCommand(friendCommand);
        CommandManager.getInstance().registerCommand(friendCommand);
        registerCommand(new IgnoreCommand());
        registerCommand(new ResidentClaimTransferCommand(this));
    }

    @Override
    public boolean run(Player p, String base, String alias, String... args) {
        if (args.length == 1) {
            String path = base != null && !base.isEmpty() ? (base + " " + alias) : alias;
            return showCommand.execute(p, path, "show", args);
        }
        return false;
    }
}
