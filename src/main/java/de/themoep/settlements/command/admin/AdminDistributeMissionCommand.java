package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.TargetParser;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.missions.MissionTaker;
import de.themoep.settlements.utils.ComponentUtils;
import de.themoep.settlements.utils.Targetable;
import org.bukkit.command.CommandSender;

public class AdminDistributeMissionCommand extends SubCommand implements TargetParser {
    public AdminDistributeMissionCommand(SubCommand parent) {
        super(parent, "distributemission <s:settlement|r:resident>");
        setPermission("settlements.admin.command.distributemission");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        Targetable targetable = getTarget(sender, args[0]);
        if (targetable instanceof MissionTaker<?> taker) {
            MissionProvider<?> provider = taker.getMissionProvider();
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
                sender.sendMessage(ComponentUtils.parseComponent(targetable.getName() + " isn't in settlement/empire."));
            }
        } else {
            return false;
        }

        return true;
    }
}
