package me.crz.minecraft.kingdoms.command.resident;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class ResidentHelpCommand extends SubCommand {
    private StringBuilder sb;

    public ResidentHelpCommand() {
        super("Lists the sub commands of the resident command", "help");
        this.setPermission("kingdoms.user");
        (sb = new StringBuilder()).append("&8&l--------&r&c&lResident Commands&r&8&l--------");
        sb.append("\n&8[&9*&8] &b/{alias} show <name> &7- Shows you information about a resident");
        sb.append("\n&8[&9*&8] &b/{alias} friend add <name> &7- Adds a player into your friends");
        sb.append("\n&8[&9*&8] &b/{alias} friend remove <name> &7- Remove a player from your friends");
        sb.append("\n&8[&9*&8] &b/{alias} friend list &7- Shows the list of friends you have");
        sb.append("\n&8[&9*&8] &b/{alias} help &7- Shows this");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        MessageUtils.tell(player, sb.toString(), "{alias}", base);
    }
}
