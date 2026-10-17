package de.themoep.settlements.commandold.common;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Targetable;
import org.bukkit.command.CommandSender;

public interface TargetParser extends ConfigMessages {

    default Targetable getTarget(Resident resident, String input) {
        return getTarget(resident.getPlayer(), input);
    }

    default Targetable getTarget(CommandSender sender, String input) {
        Targetable target;
        if (input.startsWith("settlement:") || input.startsWith("s:")) {
            String name = input.split(":")[1];
            target = SettlementManager.getInstance().getFromName(name);
            if (target == null) {
                target = SettlementManager.getInstance().getFromName(name.replace('_', ' '));
                if (target == null) {
                    getError("settlement-doesnt-exist").tell(sender, "name", name);
                    return null;
                }
            }
        } else {
            String name = input;
            if (name.contains(":")) {
                name = name.split(":")[1];
            }
            target = ResidentManager.getInstance().getResident(name);
            if (target == null) {
                getError("player-not-found").tell(sender, "name", name);
                return null;
            }
        }
        return target;
    }
}
