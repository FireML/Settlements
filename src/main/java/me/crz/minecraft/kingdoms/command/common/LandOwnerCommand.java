package me.crz.minecraft.kingdoms.command.common;

import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

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
            target = VillageManager.getInstance().getVillageFromName(arg.split(":")[1]);
            if (target == null) {
                getError("village-doesnt-exist").tell(resident, "name", arg.split(":")[1]);
                return null;
            }
        } else {
            String name = arg;
            if (name.contains(":")) {
                name = name.split(":")[1];
            }
            Village village = VillageManager.getInstance().getVillageFromName(name);
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
