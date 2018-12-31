package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireJoinFeeCommand extends EmpireSubCommand {
    public EmpireJoinFeeCommand(SubCommand parent) {
        super(parent, "joinfee <amount>");
        registerCommand(new EmpireSubCommand(this, "set <amount>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    return false;
                }
                try {
                    long amount = Long.parseLong(args[0]);
                    resident.getVillage().getEmpire().setJoinFee(amount);
                    getMessage("success").tell(resident.getVillage().getEmpire().getOnlineResidents(),
                            "amount", String.valueOf(resident.getVillage().getEmpire().getJoinFee())
                    );
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire()));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(resident, "input", args[0]);
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            getMessage("current").tell(resident,
                    "amount", String.valueOf(resident.getVillage().getEmpire().getJoinFee())
            );
            return true;
        }
        return true;
    }
}
