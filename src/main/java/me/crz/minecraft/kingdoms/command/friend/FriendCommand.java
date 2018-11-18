package me.crz.minecraft.kingdoms.command.friend;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import org.bukkit.entity.Player;

public class FriendCommand extends PlayerSubCommand {

    public FriendCommand() {
        super(null, "friend");
        setPermission("kingdoms.user");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new FriendAddCommand(this));
        registerCommand(new FriendRemoveCommand(this));
        registerCommand(new FriendListCommand(this));
    }

    @Override
    public boolean run(final Player p, final String base, final String alias, final String... args) {
        return false;
    }
}
