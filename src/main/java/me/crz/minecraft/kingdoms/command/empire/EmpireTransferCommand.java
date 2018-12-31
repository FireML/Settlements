package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class EmpireTransferCommand extends EmpireSubCommand {
    public EmpireTransferCommand(SubCommand parent) {
        super(parent, "transfer <player>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Village targetVillage = VillageManager.getInstance().getVillageFromName(args[0]);
            if (targetVillage == null) {
                res.tell(getError("village-doesnt-exist"), "name", args[0]);
                return;
            }
            Empire empire = res.getVillage().getEmpire();
            if (!empire.isMember(targetVillage)) {
                res.tell(getError("not-in-empire"), "name", empire.getName());
                return;
            }
            if (empire.isCapital(targetVillage)) {
                res.tell(getError("already-capital"), "name", targetVillage.getName(), "state", targetVillage.getStateTag());
                return;
            }
            if (!targetVillage.isMaxLevel()) {
                getError("max-level-kingdom-required").tell(res,
                        "state", targetVillage.getState().getTag(),
                        "level", String.valueOf(targetVillage.getLevel())
                );
                return;
            }
            Resident oldLeader = empire.getCapital().getChief();
            oldLeader.setEmpireRank(EmpireRank.SENATOR);
            empire.addSenator(oldLeader);
            Resident newLeader = targetVillage.getChief();
            newLeader.setEmpireRank(EmpireRank.LEADER);
            empire.removeSenator(newLeader);
            empire.setCapital(targetVillage);
            DatabaseManager.getInstance().saveEmpire(empire);
            DatabaseManager.getInstance().saveResident(oldLeader, newLeader);
            MessageUtils.tellResidents(empire.getOnlineResidents(), getMessage("success"),
                    "rank", EmpireRank.LEADER.getTag(),
                    "setter", res.getName(),
                    "target", targetVillage.getName(),
                    "empire", empire.getName()
            );
        });
        return true;
    }
}
