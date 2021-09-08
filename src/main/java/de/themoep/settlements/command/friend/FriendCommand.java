package de.themoep.settlements.command.friend;

import de.themoep.settlements.command.HelpCommand;
import de.themoep.settlements.command.PlayerSubCommand;
import org.bukkit.entity.Player;

public class FriendCommand extends PlayerSubCommand {

    public FriendCommand() {
        super(null, "friend");
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
