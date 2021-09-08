package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.InviteCache;

public class EmpireInviteCommand extends EmpireSubCommand {
    public EmpireInviteCommand(SubCommand parent) {
        super(parent, "invite <settlement>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Village invitedVillage = VillageManager.getInstance().getFromName(args[0]);
        if (invitedVillage == null) {
            getError("village-doesnt-exist").tell(res, "name", args[0]);
            return true;
        }
        if (invitedVillage.hasEmpire()) {
            getMessage("already-in-empire").tell(res,
                    "name", invitedVillage.getName(),
                    "state", invitedVillage.getStateTag(),
                    "empire", invitedVillage.getEmpire().getName());
            return true;
        }
        InviteCache.getPendingEmpireInvites().put(invitedVillage.getId(), res.getVillage().getEmpire().getId());
        getMessage("received").tell(invitedVillage.getOnlineResidents(), "name", res.getVillage().getEmpire().getName());
        getMessage("joinfee").tell(invitedVillage.getOnlineResidents(), "name", res.getVillage().getEmpire().getName(), "amount", String.valueOf(res.getVillage().getEmpire().getJoinFee()));
        getMessage("alert").tell(res.getVillage().getEmpire().getOnlineResidents(), "name", invitedVillage.getName(), "state", invitedVillage.getStateTag());
        return true;
    }
}
