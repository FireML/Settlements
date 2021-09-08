package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.utils.MessageUtils;

public class EmpireKickCommand extends EmpireSubCommand {
    public EmpireKickCommand(SubCommand parent) {
        super(parent, "kick <settlement>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        if (args[0].equalsIgnoreCase(resident.getVillage().getName())) {
            getMessage("cant-kick-yourself").tell(resident);
            return true;
        }

        Village village = VillageManager.getInstance().getFromName(args[0]);
        if (village == null) {
            getError("village-doesnt-exist").tell(resident);
            return true;
        }
        if (!resident.getVillage().getEmpire().isMember(village)) {
            getError("not-in-current").tell(resident, "name", village.getName());
            return true;
        }
        if (village.isCapital()) {
            getMessage("cant-kick-capital").tell(resident);
            return true;
        }
        MessageUtils.tellResidents(village.getEmpire().getOnlineResidents(), getMessage("kicked-alert"),
                "name", village.getName(),
                "state", village.getState().getTag()
        );
        InviteCache.getPendingEmpireInvites().remove(village.getId(), village.getEmpire().getId());
        village.leaveEmpire();
        return true;
    }
}
