package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.events.LandownerChiefChangeEvent;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;

public class VillageTransferCommand extends VillageSubCommand {
    public VillageTransferCommand(SubCommand parent) {
        super(parent, "transfer <player>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident tres = ResidentManager.getInstance().getResident(args[0]);
            if (tres == null) {
                res.tell(getError("player-not-found"), "name", args[0]);
                return;
            }
            Settlement settlement = res.getSettlement();
            if (!isResident(tres, settlement)) {
                res.tell(getError("not-in-current"), "name", settlement.getName(), "state", settlement.getState().getTag());
                return;
            }
            if (settlement.isChief(tres)) {
                getError("already-chief").tell(res, "name", tres.getName());
                return;
            }
            Utils.runSync(() -> {
                Utils.callEvent(new LandownerChiefChangeEvent(settlement, settlement.getChief(), tres));
                res.setSettlementRank(SettlementRank.getChief().next());
                tres.setSettlementRank(SettlementRank.getChief());
                settlement.setChief(tres.getUUID());
                if (settlement.hasEmpire() && settlement.getEmpire().isChief(res)) {
                    res.setEmpireRank(EmpireRank.getLeader().next());
                    tres.setEmpireRank(EmpireRank.getLeader());
                }
                DatabaseManager.getInstance().saveResident(res);
                DatabaseManager.getInstance().saveResident(tres);
                DatabaseManager.getInstance().saveSettlement(settlement);
                MessageUtils.tellResidents(settlement.getOnlineResidents(), getMessage("success"),
                        "rank", SettlementRank.getChief().getTag(settlement),
                        "setter", res.getName(),
                        "target", tres.getName()
                );
            });
        });
        return true;
    }

    private boolean isResident(Resident resident, Settlement settlement) {
        return settlement.getResidentIds().contains(resident.getUUID());
    }
}
