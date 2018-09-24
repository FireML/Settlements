package me.crz.minecraft.kingdoms.command.resident;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class ResidentHelpCommand extends SubCommand
{
    StringBuilder sb;
    
    public ResidentHelpCommand() {
        super("Lists the sub commands of the resident command", new String[] { "help" });
        this.setPermission("village.user");
        (sb = new StringBuilder()).append("&8&l--------&r&c&lVillage Commands&r&8&l--------");
        sb.append("\n&8[&9*&8] &b/{alias} show <name> &7- Shows you information about a resident");
        sb.append("\n&8[&9*&8] &b/{alias} friend &7- Friend based commands");
        sb.append("\n&8[&9*&8] &b/{alias} help &7- Shows this");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        MessageUtils.tell(player, sb.toString());
    }
}
