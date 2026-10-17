package de.themoep.settlements.commandold.admin;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.commandold.common.TargetParser;

import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Targetable;
import org.bukkit.command.CommandSender;

public class AdminDistributeNewMissionsCommand extends SubCommand implements TargetParser {
    public AdminDistributeNewMissionsCommand(SubCommand parent) {
        super(parent, "distributenewmissions <s:settlement>", "newmissions");
        setPermission("settlements.admin.command.distributenewmissions");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        Targetable targetable = getTarget(sender, args[0]);
        if (targetable instanceof Settlement settlement) {
            settlement.distributeNewMissions();
            getMessage("success").tell(sender,
                    "name", targetable.getName(),
                    "state", settlement.getStateTag()
            );
            return true;
        }
        return false;
    }
}
