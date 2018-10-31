package me.crz.minecraft.kingdoms.command;

import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.friend.FriendListCommand;
import me.crz.minecraft.kingdoms.command.friend.FriendRemoveCommand;
import me.crz.minecraft.kingdoms.command.friend.FriendAddCommand;

import me.crz.minecraft.kingdoms.command.SubCommand;

public class FriendCommand extends SubCommand {

    public FriendCommand() {
        super(null, "friend");
        setPermission("kingdoms.user");
        registerCommand(new FriendAddCommand(this));
        registerCommand(new FriendRemoveCommand(this));
        registerCommand(new FriendListCommand(this));
    }

    @Override
    public boolean run(final Player p, final String base, final String alias, final String... args) {
        return false;
    }
}
