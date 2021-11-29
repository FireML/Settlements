package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.village.SettlementManager;
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
        Settlement invitedSettlement = SettlementManager.getInstance().getFromName(args[0]);
        if (invitedSettlement == null) {
            getError("settlement-doesnt-exist").tell(res, "name", args[0]);
            return true;
        }
        if (invitedSettlement.hasEmpire()) {
            getMessage("already-in-empire").tell(res,
                    "name", invitedSettlement.getName(),
                    "state", invitedSettlement.getStateTag(),
                    "empire", invitedSettlement.getEmpire().getName());
            return true;
        }
        InviteCache.getPendingEmpireInvites().put(invitedSettlement.getId(), res.getSettlement().getEmpire().getId());
        getMessage("received").tell(invitedSettlement.getOnlineResidents(), "name", res.getSettlement().getEmpire().getName());
        getMessage("joinfee").tell(invitedSettlement.getOnlineResidents(), "name", res.getSettlement().getEmpire().getName(), "amount", String.valueOf(res.getSettlement().getEmpire().getJoinFee()));
        getMessage("alert").tell(res.getSettlement().getEmpire().getOnlineResidents(), "name", invitedSettlement.getName(), "state", invitedSettlement.getStateTag());
        return true;
    }
}
