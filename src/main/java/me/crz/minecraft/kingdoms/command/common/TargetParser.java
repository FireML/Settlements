package me.crz.minecraft.kingdoms.command.common;

import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.ConfigMessages;
import me.crz.minecraft.kingdoms.utils.Targetable;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
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
        } else if (input.startsWith("village:") || input.startsWith("v:") || input.startsWith("settlement:") || input.startsWith("s:")) {
            target = VillageManager.getInstance().getVillageFromName(input.split(":")[1]);
            if (target == null) {
                getError("village-doesnt-exist").tell(player, "name", input.split(":")[1]);
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
