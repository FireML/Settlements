package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.land.events.LandownerChiefChangeEvent;
import de.themoep.settlements.rank.EmpireRank;
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
        Village targetVillage = VillageManager.getInstance().getFromName(args[0]);
        if (targetVillage == null) {
            res.tell(getError("village-doesnt-exist"), "name", args[0]);
            return true;
        }
        Empire empire = res.getVillage().getEmpire();
        if (!empire.isMember(targetVillage)) {
            res.tell(getError("not-in-empire"), "name", empire.getName());
            return true;
        }
        if (empire.isCapital(targetVillage)) {
            res.tell(getError("already-capital"), "name", targetVillage.getName(), "state", targetVillage.getStateTag());
            return true;
        }
        if (!targetVillage.isMaxLevel()) {
            getError("max-level-kingdom-required").tell(res,
                    "state", targetVillage.getState().getTag(),
                    "level", String.valueOf(targetVillage.getLevel())
            );
            return true;
        }
        Resident oldLeader = empire.getCapital().getChief();
        Resident newLeader = targetVillage.getChief();
        Utils.callEvent(new LandownerChiefChangeEvent(empire, oldLeader, newLeader));
        oldLeader.setEmpireRank(EmpireRank.getLeader().next());
        newLeader.setEmpireRank(EmpireRank.getLeader());
        empire.setCapital(targetVillage);
        DatabaseManager.getInstance().saveEmpire(empire);
        DatabaseManager.getInstance().saveResident(oldLeader, newLeader);
        MessageUtils.tellResidents(empire.getOnlineResidents(), getMessage("success"),
                "rank", EmpireRank.getLeader().getTag(),
                "setter", res.getName(),
                "target", targetVillage.getName(),
                "empire", empire.getName()
        );
        return true;
    }
}
