package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.rank.VillageRank;
import de.themoep.settlements.utils.Utils;

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
            village = VillageManager.getInstance().getFromName(name);
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
