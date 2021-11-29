package de.themoep.settlements.command.common;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Targetable;
import org.bukkit.entity.Player;

public interface TargetParser extends ConfigMessages {

    default Targetable getTarget(Resident resident, String[] args) {
        return getTarget(resident.getPlayer(), args);
    }

    default Targetable getTarget(Player player, String[] args) {
        return getTarget(player, String.join(" ", args));
    }

    default Targetable getTarget(Player player, String input) {
        Targetable target;
        if (input.startsWith("empire:") || input.startsWith("e:")) {
            target = EmpireManager.getInstance().getEmpireFromName(input.split(":")[1]);
            if (target == null) {
                getError("empire-doesnt-exist").tell(player, "name", input.split(":")[1]);
                return null;
            }
        } else if (input.startsWith("settlement:") || input.startsWith("s:")) {
            target = SettlementManager.getInstance().getFromName(input.split(":")[1]);
            if (target == null) {
                getError("settlement-doesnt-exist").tell(player, "name", input.split(":")[1]);
                return null;
            }
        } else {
            String name = input;
            if (name.contains(":")) {
                name = name.split(":")[1];
            }
            target = ResidentManager.getInstance().getResident(name);
            if (target == null) {
                getError("player-not-found").tell(player, "name", name);
                return null;
            }
        }
        return target;
    }
}
