package me.crz.minecraft.kingdoms.command.resident;

import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.resident.friend.FriendListCommand;
import me.crz.minecraft.kingdoms.command.resident.friend.FriendRemoveCommand;
import me.crz.minecraft.kingdoms.command.resident.friend.FriendAddCommand;

import me.crz.minecraft.kingdoms.command.SubCommand;

public class ResidentFriendCommand extends SubCommand {

    public ResidentFriendCommand(SubCommand parent) {
        super(parent, "friend");
        setPermission("kingdoms.user");
        setDeepUsage(true);
        registerCommand(new FriendAddCommand(this));
        registerCommand(new FriendRemoveCommand(this));
        registerCommand(new FriendListCommand(this));
    }

    @Override
    public boolean run(final Player p, final String base, final String alias, final String... args) {
        return false;
    }
}
