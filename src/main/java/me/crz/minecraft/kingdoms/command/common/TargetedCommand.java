package me.crz.minecraft.kingdoms.command.common;

import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;

public interface TargetedCommand extends ConfigMessages {

    default Targetable getTarget(Resident resident, String[] args) {
        Targetable target;
        if (args[0].startsWith("empire:") || args[0].startsWith("e:")) {
            target = EmpireManager.getInstance().getEmpireFromName(args[0].split(":")[1]);
            if (target == null) {
                getError("empire-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                return null;
            }
        } else if (args[0].startsWith("village:") || args[0].startsWith("v:") || args[0].startsWith("settlement:") || args[0].startsWith("s:")) {
            target = VillageManager.getInstance().getVillageFromName(args[0].split(":")[1]);
            if (target == null) {
                getError("village-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                return null;
            }
        } else {
            String name = args[0];
            if (name.contains(":")) {
                name = name.split(":")[1];
            }
            target = ResidentManager.getInstance().getResident(name);
            if (target == null) {
                getError("player-not-found").tell(resident, "name", name);
                return null;
            }
        }
        return target;
    }
}
