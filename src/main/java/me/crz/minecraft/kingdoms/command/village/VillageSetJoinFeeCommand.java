package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class VillageSetJoinFeeCommand extends VillageSubCommand {
    public VillageSetJoinFeeCommand(SubCommand parent) {
        super(parent, "setjoinfee <amount>", VillageRank.CHIEF, "joinfee");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        long amount = 0;
        if (args.length > 0) {
            try {
                amount = Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(resident, "input", args[0]);
                return false;
            }
        }
        resident.getVillage().setJoinFee(amount);
        getMessage("success").tell(resident.getVillage().getOnlineResidents(), "amount", String.valueOf(amount));
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
        return true;
    }
}
