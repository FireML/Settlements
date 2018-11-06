package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class VillageUnAllyCommand extends VillageSubCommand {
    public VillageUnAllyCommand(SubCommand parent) {
        super(parent, "unally <name>", VillageRank.ELDER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Village village = resident.getVillage();
        Village targetVillage = VillageManager.getInstance().getVillageFromName(args[0].toLowerCase());
        if (targetVillage == null) {
            resident.tell(getError("village-doesnt-exist"), "name", args[0]);
        } else if (targetVillage.getName().equals(village.getName())) {
            resident.tell(getError("cant-unally-yourself"));
        } else if (!village.isAlly(targetVillage)) {
            resident.tell(getError("not-allied"), "name", targetVillage.getName(), "state", targetVillage.getState().getTag());
        } else {
            village.removeAlly(targetVillage);
            targetVillage.removeAlly(village);
            LandManager.getInstance().checkUnallyDistance(village, targetVillage);
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveVillage(targetVillage);
                DatabaseManager.getInstance().saveVillage(village);
            });
            MessageUtils.tellResidents(targetVillage.getOnlineResidents(), getMessage("success"), "name", village.getName());
            MessageUtils.tellResidents(village.getOnlineResidents(), getMessage("success"), "name", targetVillage.getName());
        }
        return true;
    }
}
