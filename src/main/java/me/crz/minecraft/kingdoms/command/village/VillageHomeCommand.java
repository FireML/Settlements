package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class VillageHomeCommand extends ResidentSubCommand {
    public VillageHomeCommand(SubCommand parent) {
        super(parent, "home [<name>]");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Village village;
        if (args.length < 1) {
            if (!resident.hasVillage()) {
                getError("village-required").tell(resident);
                return false;
            } else if (resident.getVillage().getHome() == null) {
                getMessage("own-no-home").tell(resident, "chief", VillageRank.getChief().getTag(resident.getVillage()));
                return true;
            } else {
                village = resident.getVillage();
            }
        } else {
            String name = String.join(" ", args);
            village = VillageManager.getInstance().getVillageFromName(name);
            if (village == null) {
                getError("village-doesnt-exist").tell(resident, "name", name);
                return true;
            }
        }

        if (!village.isMember(resident)
                && (village.getAccess() == LandAccess.PRIVATE
                || (village.getAccess() == LandAccess.ALLY && !village.isAlly(resident))
                || (village.getAccess() == LandAccess.TRUSTED && !village.isTrusted(resident)))) {
            getMessage("no-access").tell(resident, "state", village.getStateTag());
            return true;
        }

        if (village.getHome() != null) {
            Utils.teleport(this, resident.getPlayer(), village.getHome().toBukkitLocation(), village.getStateKey(),
                    "state", village.getStateKey(), "name", village.getName());
        } else {
            getMessage("no-home").tell(resident, "state", village.getStateTag());
        }
        return true;
    }
}
