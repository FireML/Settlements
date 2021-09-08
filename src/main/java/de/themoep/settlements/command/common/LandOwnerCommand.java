package de.themoep.settlements.command.common;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
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
        } else if (arg.startsWith("village:") || arg.startsWith("v:") || arg.startsWith("settlement:") || arg.startsWith("s:")) {
            target = VillageManager.getInstance().getFromName(arg.split(":")[1]);
            if (target == null) {
                getError("village-doesnt-exist").tell(resident, "name", arg.split(":")[1]);
                return null;
            }
        } else {
            String name = arg;
            if (name.contains(":")) {
                name = name.split(":")[1];
            }
            Village village = VillageManager.getInstance().getFromName(name);
            Empire empire = EmpireManager.getInstance().getEmpireFromName(name);
            if (village != null && empire != null) {
                getError("ambiguous-target").tell(resident, "name", name);
                return null;
            } else if (village != null) {
                target = village;
            } else if (empire != null) {
                target = empire;
            }
            if (target == null) {
                getError("village-doesnt-exist").tell(resident, "name", name);
                return null;
            }
        }
        return target;
    }
}
