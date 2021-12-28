package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.TargetParser;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.utils.Targetable;
import org.bukkit.command.CommandSender;

public class AdminDistributeNewMissionsCommand extends SubCommand implements TargetParser {
    public AdminDistributeNewMissionsCommand(SubCommand parent) {
        super(parent, "distributenewmissions <s:settlement|e:empire>", "newmissions");
        setPermission("settlements.admin.command.distributenewmissions");
    }

    @Override
    public boolean run(CommandSender sender, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        Targetable targetable = getTarget(sender, args[0]);
        if (targetable instanceof MissionProvider && targetable instanceof LandOwner) {
            ((MissionProvider<?>) targetable).distributeNewMissions();
            getMessage("success").tell(sender,
                    "name", targetable.getName(),
                    "state", ((LandOwner<?, ?>) targetable).getStateTag()
            );
            return true;
        }
        return false;
    }
}
