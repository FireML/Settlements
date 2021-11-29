package de.themoep.settlements.command.common;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.utils.ConfigMessages;

public interface LandOwnerCommand extends ConfigMessages {
    default LandOwner getTarget(Resident resident, String arg) {
        LandOwner target = null;
        if (arg.startsWith("empire:") || arg.startsWith("e:")) {
            target = EmpireManager.getInstance().getEmpireFromName(arg.split(":")[1]);
            if (target == null) {
                getError("empire-doesnt-exist").tell(resident, "name", arg.split(":")[1]);
                return null;
            }
        } else if (arg.startsWith("settlement:") || arg.startsWith("s:")) {
            target = SettlementManager.getInstance().getFromName(arg.split(":")[1]);
            if (target == null) {
                getError("settlement-doesnt-exist").tell(resident, "name", arg.split(":")[1]);
                return null;
            }
        } else {
            String name = arg;
            if (name.contains(":")) {
                name = name.split(":")[1];
            }
            Settlement settlement = SettlementManager.getInstance().getFromName(name);
            Empire empire = EmpireManager.getInstance().getEmpireFromName(name);
            if (settlement != null && empire != null) {
                getError("ambiguous-target").tell(resident, "name", name);
                return null;
            } else if (settlement != null) {
                target = settlement;
            } else if (empire != null) {
                target = empire;
            }
            if (target == null) {
                getError("settlement-doesnt-exist").tell(resident, "name", name);
                return null;
            }
        }
        return target;
    }
}
