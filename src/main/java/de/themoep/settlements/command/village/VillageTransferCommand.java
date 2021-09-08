package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.events.LandownerChiefChangeEvent;
import de.themoep.settlements.rank.EmpireRank;
import de.themoep.settlements.rank.VillageRank;
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
            Village village = res.getVillage();
            if (!isResident(tres, village)) {
                res.tell(getError("not-in-current"), "name", village.getName(), "state", village.getState().getTag());
                return;
            }
            if (village.isChief(tres)) {
                getError("already-chief").tell(res, "name", tres.getName());
                return;
            }
            Utils.runSync(() -> {
                Utils.callEvent(new LandownerChiefChangeEvent(village, village.getChief(), tres));
                res.setVillageRank(VillageRank.getChief().next());
                tres.setVillageRank(VillageRank.getChief());
                village.setChief(tres.getUUID());
                if (village.hasEmpire() && village.getEmpire().isChief(res)) {
                    res.setEmpireRank(EmpireRank.getLeader().next());
                    tres.setEmpireRank(EmpireRank.getLeader());
                }
                DatabaseManager.getInstance().saveResident(res);
                DatabaseManager.getInstance().saveResident(tres);
                DatabaseManager.getInstance().saveSettlement(village);
                MessageUtils.tellResidents(village.getOnlineResidents(), getMessage("success"),
                        "rank", VillageRank.getChief().getTag(village),
                        "setter", res.getName(),
                        "target", tres.getName()
                );
            });
        });
        return true;
    }

    private boolean isResident(Resident resident, Village village) {
        return village.getResidentIds().contains(resident.getUUID());
    }
}
