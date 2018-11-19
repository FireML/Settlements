package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;

public class VillageTransferCommand extends VillageSubCommand {
    public VillageTransferCommand(SubCommand parent) {
        super(parent, "transfer <player>", VillageRank.CHIEF);
        setPermission("kingdoms.user");
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
                res.setVillageRank(VillageRank.ELDER);
                tres.setVillageRank(VillageRank.CHIEF);
                village.setChief(tres.getUUID());
                Utils.runAsync(() -> {
                    DatabaseManager.getInstance().saveResident(res);
                    DatabaseManager.getInstance().saveResident(tres);
                    DatabaseManager.getInstance().saveVillage(village);
                });
                MessageUtils.tellResidents(village.getOnlineResidents(), getMessage("success"),
                        "rank", VillageRank.CHIEF.getTag(village),
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
