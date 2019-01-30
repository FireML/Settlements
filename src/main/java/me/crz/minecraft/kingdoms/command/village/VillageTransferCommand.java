package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;

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
                res.setVillageRank(VillageRank.getChief().next());
                tres.setVillageRank(VillageRank.getChief());
                village.setChief(tres.getUUID());
                if (village.hasEmpire() && village.getEmpire().isChief(res)) {
                    res.setEmpireRank(EmpireRank.getLeader().next());
                    village.getEmpire().addSenator(res);
                    tres.setEmpireRank(EmpireRank.getLeader());
                    village.getEmpire().removeSenator(tres);
                }
                Utils.runAsync(() -> {
                    DatabaseManager.getInstance().saveResident(res);
                    DatabaseManager.getInstance().saveResident(tres);
                    DatabaseManager.getInstance().saveVillage(village);
                });
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
