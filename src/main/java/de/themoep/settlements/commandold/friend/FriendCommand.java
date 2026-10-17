package de.themoep.settlements.commandold.friend;

import de.themoep.settlements.commandold.HelpCommand;
import de.themoep.settlements.commandold.PlayerSubCommand;
import org.bukkit.entity.Player;

public class FriendCommand extends PlayerSubCommand {

    public FriendCommand() {
        super(null, "friend");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new FriendAddCommand(this));
        registerCommand(new FriendAcceptCommand(this));
        registerCommand(new FriendRemoveCommand(this));
        registerCommand(new FriendListCommand(this));
        registerCommand(new FriendDenyCommand(this));
    }

    @Override
    public boolean run(final Player p, final String base, final String alias, final String... args) {
        return false;
    }
}
