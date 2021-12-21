package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

public class EmpireAuctionTaxCommand extends EmpireSubCommand {
    public EmpireAuctionTaxCommand(SubCommand parent) {
        super(parent, "auctiontax [set <amount>]");
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
                    if (Config.Empire.MAX_TAX > -1 && amount > Config.Empire.MAX_AUCTION_TAX) {
                        getMessage("above-max").tell(resident,
                                "amount", String.valueOf(amount),
                                "max", String.valueOf(Config.Empire.MAX_AUCTION_TAX)
                        );
                        return true;
                    }
                    resident.getSettlement().getEmpire().setAuctionTax(amount);
                    resident.getSettlement().getEmpire().save();
                    MessageUtils.notify(resident.getSettlement().getEmpire(), resident.getSettlement().getEmpire().getResidents(), getMessage("success"),
                            "state", resident.getSettlement().getStateTag(),
                            "amount", Utils.CURRENCY_FORMAT.format(amount)
                    );
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
            getMessage("current").tell(resident, "amount", Utils.CURRENCY_FORMAT.format(resident.getSettlement().getEmpire().getAuctionTax()));
            return true;
        }
        return false;
    }
}
