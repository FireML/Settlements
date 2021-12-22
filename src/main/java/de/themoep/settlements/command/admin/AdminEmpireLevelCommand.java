package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.SettlementState;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class AdminEmpireLevelCommand extends SubCommand {
    public AdminEmpireLevelCommand(SubCommand parent) {
        super(parent, "empirelevel <empire> <level> [<triggerevents>]", "setlevel");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 3) {
            return false;
        }
        Empire empire = EmpireManager.getInstance().getEmpireFromName(args[0]);
        if (empire == null) {
            getError("empire-doesnt-exist").tell(sender, "name", args[0]);
            return true;
        }
        try {
            int level = Integer.parseInt(args[2]);

            int oldRadius = empire.getWorldRadius();
            empire.setLevel(level);

            sender.sendMessage(ChatColor.RED + "Set " + empire.getName() + " to " + level);

            if (!"false".equalsIgnoreCase(args[3])) {
                EmpireManager.getInstance().onLevelUp(empire, empire.getChief(), oldRadius);
                sender.sendMessage(ChatColor.RED + "Triggered level up events!");
            }
            empire.save();
        } catch (NumberFormatException e) {
            getError("not-a-number").tell(sender, "input", args[2]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
