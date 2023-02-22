package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.settlement.SettlementSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.JoinType;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

public class EmpireJoinCommand extends SettlementSubCommand {
    public EmpireJoinCommand(SubCommand parent) {
        super(parent, "join <name>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Empire joinEmpire = EmpireManager.getInstance().getEmpireFromName(args[0]);
        if (joinEmpire == null || joinEmpire.isAbandoned()) {
            resident.tell(getError("empire-doesnt-exist"), "name", args[0]);
        } else if (joinEmpire.getId().isNull()) {
            getError("missing-empire-id").tell(resident, "name", joinEmpire.getName());
        } else if (joinEmpire.getJoinType() != JoinType.ANYONE && !Utils.settlementHasInvite(resident.getSettlement(), joinEmpire)) {
            resident.tell(getMessage("no-invite"),
                    "name", joinEmpire.getName(),
                    "state", resident.getSettlement().getState().getTag()
            );
        } else if (joinEmpire.isFull()) {
            resident.tell(getMessage("is-full"),
                    "name", joinEmpire.getName(),
                    "state", resident.getSettlement().getState().getTag()
            );
        } else if (joinEmpire.getJoinFee() > 0 && !resident.getSettlement().has(joinEmpire.getJoinFee())) {
            resident.tell(getError("not-enough-money"),
                    "name", joinEmpire.getName(),
                    "amount", String.valueOf(joinEmpire.getJoinFee()),
                    "state", resident.getSettlement().getState().getTag()
            );
        } else {
            if (joinEmpire.getJoinFee() > 0) {
                resident.getSettlement().removeFromBank(joinEmpire.getJoinFee());
                joinEmpire.addToBank(joinEmpire.getJoinFee());
                getMessage("money-paid").tell(resident, "amount", String.valueOf(joinEmpire.getJoinFee()));
                DatabaseManager.getInstance().saveEmpire(joinEmpire);
                DatabaseManager.getInstance().log(joinEmpire, resident, "joinfee", "cash", joinEmpire.getJoinFee());
            }
            joinEmpire.addMember(resident.getSettlement());
            getMessage("success").tell(resident.getSettlement().getOnlineResidents(),
                    "name", joinEmpire.getName(),
                    "state", resident.getSettlement().getState().getTag()
            );
            MessageUtils.tellResidents(joinEmpire.getOnlineResidents(), getMessage("new-member"),
                    "name", resident.getSettlement().getName(),
                    "state", resident.getSettlement().getState().getTag()
            );
        }
        return true;
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (resident.hasEmpire()) {
            getError("no-empire-required").tell(resident);
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return !resident.hasEmpire() && super.hasAccess(resident);
    }
}
