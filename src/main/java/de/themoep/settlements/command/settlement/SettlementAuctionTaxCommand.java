package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

public class SettlementAuctionTaxCommand extends SettlementSubCommand {
    public SettlementAuctionTaxCommand(SubCommand parent) {
        super(parent, "auctiontax [set <amount>|none]");
        registerCommand(new SettlementSubCommand(this, "set <amount>") {
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
                    if (Config.Settlement.MAX_TAX > -1 && amount > Config.Settlement.MAX_AUCTION_TAX) {
                        getMessage("above-max").tell(resident,
                                "amount", String.valueOf(amount),
                                "max", String.valueOf(Config.Settlement.MAX_AUCTION_TAX)
                        );
                        return true;
                    }
                    resident.getSettlement().setAuctionTax(amount);
                    resident.getSettlement().save();
                    MessageUtils.notify(resident.getSettlement(), resident.getSettlement().getResidents(), getMessage("success"),
                            "state", resident.getSettlement().getStateTag(),
                            "amount", Utils.toPercentage(resident.getSettlement().getAuctionTax())
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
            getMessage("current").tell(resident,
                    "state", resident.getSettlement().getStateTag(),
                    "amount", Utils.toPercentage(resident.getSettlement().getAuctionTax())
            );
            return true;
        }
        return false;
    }
}
