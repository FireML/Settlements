package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
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

        if (args[0].equalsIgnoreCase(resident.getSettlement().getName())) {
            getMessage("cant-kick-yourself").tell(resident);
            return true;
        }

        Settlement settlement = SettlementManager.getInstance().getFromName(args[0]);
        if (settlement == null) {
            getError("settlement-doesnt-exist").tell(resident);
            return true;
        }
        if (!resident.getSettlement().getEmpire().isMember(settlement)) {
            getError("not-in-current").tell(resident, "name", settlement.getName());
            return true;
        }
        if (settlement.isCapital()) {
            getMessage("cant-kick-capital").tell(resident);
            return true;
        }
        MessageUtils.tellResidents(settlement.getEmpire().getOnlineResidents(), getMessage("kicked-alert").getReplacedComponent(
                "name", settlement.getName(),
                "state", settlement.getState().getTag()
        ));
        InviteCache.getPendingEmpireInvites().remove(settlement.getId(), settlement.getEmpire().getId());
        settlement.leaveEmpire();
        return true;
    }
}
