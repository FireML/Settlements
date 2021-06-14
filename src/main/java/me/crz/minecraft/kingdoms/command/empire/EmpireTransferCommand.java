package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.land.events.LandownerChiefChangeEvent;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class EmpireTransferCommand extends EmpireSubCommand {
    public EmpireTransferCommand(SubCommand parent) {
        super(parent, "transfer <settlement>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Village targetVillage = VillageManager.getInstance().getVillageFromName(args[0]);
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
