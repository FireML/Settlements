package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.land.events.LandownerChiefChangeEvent;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

public class EmpireTransferCommand extends EmpireSubCommand {
    public EmpireTransferCommand(SubCommand parent) {
        super(parent, "transfer <settlement>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Settlement targetSettlement = SettlementManager.getInstance().getFromName(args[0]);
        if (targetSettlement == null) {
            res.tell(getError("settlement-doesnt-exist").getComponent(), "name", args[0]);
            return true;
        }
        Empire empire = res.getSettlement().getEmpire();
        if (!empire.isMember(targetSettlement)) {
            res.tell(getError("not-in-empire").getComponent(), "name", empire.getName());
            return true;
        }
        if (empire.isCapital(targetSettlement)) {
            res.tell(getError("already-capital").getComponent(), "name", targetSettlement.getName(), "state", targetSettlement.getStateTag());
            return true;
        }
        if (!targetSettlement.isMaxLevel()) {
            getError("max-level-kingdom-required").tell(res,
                    "state", targetSettlement.getState().getTag(),
                    "level", String.valueOf(targetSettlement.getLevel())
            );
            return true;
        }
        Resident oldLeader = empire.getCapital().getChief();
        Resident newLeader = targetSettlement.getChief();
        Utils.callEvent(new LandownerChiefChangeEvent(empire, oldLeader, newLeader));
        oldLeader.setEmpireRank(EmpireRank.getLeader().next());
        newLeader.setEmpireRank(EmpireRank.getLeader());
        empire.setCapital(targetSettlement);
        DatabaseManager.getInstance().saveEmpire(empire);
        oldLeader.save();
        newLeader.save();
        MessageUtils.tellResidents(empire.getOnlineResidents(), getMessage("success").getReplacedComponent(
                "rank", EmpireRank.getLeader().getTag(),
                "setter", res.getName(),
                "target", targetSettlement.getName(),
                "empire", empire.getName()
        ));
        return true;
    }
}
