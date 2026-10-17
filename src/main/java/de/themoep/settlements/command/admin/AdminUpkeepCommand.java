package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.TargetParser;

import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Targetable;
import org.bukkit.command.CommandSender;

public class AdminUpkeepCommand extends SubCommand implements TargetParser {
    public AdminUpkeepCommand(SubCommand parent) {
        super(parent, "upkeep <s:settlement>", "triggerupkeep");
        setPermission("settlements.admin.command.upkeep");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        Targetable targetable = getTarget(sender, args[0]);
        if (targetable instanceof Settlement settlement) {
            settlement.doUpkeep();
            getMessage("success").tell(sender,
                    "name", targetable.getName(),
                    "state", settlement.getStateTag()
            );
            return true;
        }
        return false;
    }
}
