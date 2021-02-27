package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.TaxDebtAction;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.resident.Resident;

public class EmpireTaxCommand extends EmpireSubCommand {
    public EmpireTaxCommand(SubCommand parent) {
        super(parent, "tax [set <amount>|action kick|none]");
        registerCommand(new EmpireSubCommand(this, "set <amount>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    return false;
                }
                try {
                    long amount = Long.parseLong(args[0]);
                    if (amount < 0) {
                        getMessage("below-zero").tell(resident,
                                "amount", String.valueOf(amount)
                        );
                        return true;
                    }
                    if (Config.Empire.MAX_TAX > -1 && amount > Config.Empire.MAX_TAX) {
                        getMessage("above-max").tell(resident,
                                "amount", String.valueOf(amount),
                                "max", String.valueOf(Config.Empire.MAX_TAX)
                        );
                        return true;
                    }
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
        registerCommand(new EmpireSubCommand(this, "action kick|none") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    return false;
                }
                try {
                    TaxDebtAction taxDebtAction = TaxDebtAction.valueOf(args[0].toUpperCase());
                    if (taxDebtAction != resident.getVillage().getEmpire().getTaxDebtAction()) {
                        resident.getVillage().setTaxDebtAction(taxDebtAction);
                        getMessage("success").tell(resident.getVillage().getEmpire().getOnlineResidents(), "action", taxDebtAction.getTag());
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(resident.getVillage().getEmpire()));
                    } else {
                        getMessage("already-set").tell(resident, "action", taxDebtAction.getTag());
                    }
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
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
