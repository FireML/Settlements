package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireSetJoinFeeCommand extends EmpireSubCommand {
    public EmpireSetJoinFeeCommand(SubCommand parent) {
        super(parent, "setjoinfee <amount>", EmpireRank.LEADER, "joinfee");
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
        resident.getVillage().getEmpire().setJoinFee(amount);
        getMessage("success").tell(resident.getVillage().getEmpire().getOnlineResidents(), "amount", String.valueOf(amount));
        Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire()));
        return true;
    }
}
