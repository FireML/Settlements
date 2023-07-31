package de.themoep.settlements.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PlayerSubCommand extends SubCommand {

    public PlayerSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    public abstract boolean run(Player player, String base, String alias, String... args);

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (sender instanceof Player player) {
            return run(player, base, alias, args);
        }
        getError("only-player").tell(sender);
        return true;
    }

    @Override
    public boolean hasAccess(CommandSender sender) {
        return super.hasAccess(sender) && sender instanceof Player player && hasAccess(player);
    }

    @Override
    public boolean testAccess(CommandSender sender) {
        return sender instanceof Player player && testAccess(player) && super.testAccess(sender);
    }

    public boolean testAccess(Player player) {
        return true;
    }

    public boolean hasAccess(Player player) {
        return true;
    }
}
