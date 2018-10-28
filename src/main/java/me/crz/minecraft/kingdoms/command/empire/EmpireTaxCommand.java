package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireTaxCommand extends EmpireSubCommand {
    public EmpireTaxCommand(SubCommand parent) {
        super(parent, "tax [set <amount>]", EmpireRank.NONE);
        setPermission("kingdoms.user");
        registerCommand(new EmpireSubCommand(this, "set <amount>", EmpireRank.LEADER) {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                try {
                    long amount = Long.parseLong(args[0]);
                    resident.getVillage().getEmpire().setTax(amount);
                    getMessage("success").tell(resident.getVillage().getEmpire().getOnlineResidents(), "amount", String.valueOf(amount));
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
            getMessage("current").tell(resident, "amount", String.valueOf(resident.getVillage().getEmpire().getTax()));
            return true;
        }
        return false;
    }
}
