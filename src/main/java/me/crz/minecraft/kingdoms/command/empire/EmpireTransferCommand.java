package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
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
        super(parent, "transfer <player>", EmpireRank.LEADER);
        setPermission("kingdoms.user");
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
            if (!isMember(targetVillage, empire)) {
                res.tell(getError("not-in-empire"), "name", empire.getName());
                return;
            }
            empire.setCapital(targetVillage);
            DatabaseManager.getInstance().saveEmpire(empire);
            MessageUtils.tellResidents(empire.getOnlineResidents(), getMessage("success"),
                    "setter", res.getName(),
                    "target", targetVillage.getName(),
                    "empire", empire.getName()
            );
        });
        return true;
    }

    private boolean isMember(Village village, Empire empire) {
        return empire.getMemberIds().contains(village.getId());
    }
}
