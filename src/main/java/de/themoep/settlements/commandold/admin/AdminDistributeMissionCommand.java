package de.themoep.settlements.commandold.admin;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.commandold.common.TargetParser;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.Targetable;
import org.bukkit.command.CommandSender;

public class AdminDistributeMissionCommand extends SubCommand implements TargetParser {
    public AdminDistributeMissionCommand(SubCommand parent) {
        super(parent, "distributemission <r:resident>");
        setPermission("settlements.admin.command.distributemission");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        Targetable targetable = getTarget(sender, args[0]);
        if (targetable instanceof Resident taker) {
            Settlement provider = taker.getMissionProvider();
            if (provider != null) {
                if (taker.distributeNewMissions() > 0) {
                    getMessage("success").tell(sender,
                            "name", targetable.getName(),
                            "amount", String.valueOf(taker.getMissionData().size()));
                } else {
                    getMessage("none-distributed").tell(sender,
                            "name", targetable.getName());
                }
            } else {
                sender.sendMessage(ComponentUtils.parseComponent(targetable.getName() + " isn't in settlement."));
            }
        } else {
            return false;
        }

        return true;
    }
}
