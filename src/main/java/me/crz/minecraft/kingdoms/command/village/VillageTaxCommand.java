package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.TaxDebtAction;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.resident.Resident;

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
                    resident.getVillage().setTax(amount);
                    getMessage("success").tell(resident.getVillage().getOnlineResidents(),
                            "state", resident.getVillage().getStateTag(),
                            "amount", String.valueOf(amount)
                    );
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
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
                    if (taxDebtAction != resident.getVillage().getTaxDebtAction()) {
                        resident.getVillage().setTaxDebtAction(taxDebtAction);
                        getMessage("success").tell(resident.getVillage().getOnlineResidents(),
                                "state", resident.getVillage().getStateTag(),
                                "action", taxDebtAction.getTag()
                        );
                        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
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
                    "state", resident.getVillage().getStateTag(),
                    "amount", String.valueOf(resident.getVillage().getTax())
            );
            return true;
        }
        return false;
    }
}
