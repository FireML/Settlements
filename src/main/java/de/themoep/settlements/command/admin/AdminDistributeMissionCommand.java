package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.TargetParser;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.DatabaseSerializable;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.missions.MissionTaker;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Targetable;
import org.bukkit.command.CommandSender;

public class AdminDistributeMissionCommand extends SubCommand implements TargetParser {
    public AdminDistributeMissionCommand(SubCommand parent) {
        super(parent, "distributemission <s:settlement|r:resident>", "distributemission");
        setPermission("settlements.admin.command.distributemission");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        Targetable targetable = getTarget(sender, args[0]);
        if (targetable instanceof MissionTaker) {
            MissionProvider provider = ((MissionTaker) targetable).getMissionProvider();
            if (provider != null) {
                if (provider.distributeNewMissions((MissionTaker) targetable)) {
                    getMessage("success").tell(sender,
                            "name", targetable.getName(),
                            "amount", String.valueOf(((MissionTaker<?>) targetable).getMissionData().size()));
                } else {
                    getMessage("none-distributed").tell(sender,
                            "name", targetable.getName());
                }
            } else {
                sender.sendMessage(targetable.getName() + " isn't in settlement/empire.");
            }
        } else {
            return false;
        }

        return true;
    }
}
