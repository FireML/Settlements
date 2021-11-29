package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.TaxDebtAction;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

public class VillageTaxCommand extends VillageSubCommand {
    public VillageTaxCommand(SubCommand parent) {
        super(parent, "tax [set <amount>|action kick|none]");
        registerCommand(new VillageSubCommand(this, "set <amount>") {
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
                    if (Config.Settlement.MAX_TAX > -1 && amount > Config.Settlement.MAX_TAX) {
                        getMessage("above-max").tell(resident,
                                "amount", String.valueOf(amount),
                                "max", String.valueOf(Config.Settlement.MAX_TAX)
                        );
                        return true;
                    }
                    resident.getSettlement().setTax(amount);
                    getMessage("success").tell(resident.getSettlement().getOnlineResidents(),
                            "state", resident.getSettlement().getStateTag(),
                            "amount", String.valueOf(amount)
                    );
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveSettlement(resident.getSettlement()));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(resident, "input", args[0]);
                    return false;
                }
                return true;
            }
        });
        registerCommand(new VillageSubCommand(this, "action kick|none") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    return false;
                }
                try {
                    TaxDebtAction taxDebtAction = TaxDebtAction.valueOf(args[0].toUpperCase());
                    if (taxDebtAction != resident.getSettlement().getTaxDebtAction()) {
                        resident.getSettlement().setTaxDebtAction(taxDebtAction);
                        getMessage("success").tell(resident.getSettlement().getOnlineResidents(),
                                "state", resident.getSettlement().getStateTag(),
                                "action", taxDebtAction.getTag()
                        );
                        DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
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
            getMessage("current").tell(resident,
                    "state", resident.getSettlement().getStateTag(),
                    "amount", String.valueOf(resident.getSettlement().getTax())
            );
            return true;
        }
        return false;
    }
}
