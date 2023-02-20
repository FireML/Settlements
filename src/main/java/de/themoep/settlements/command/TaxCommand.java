package de.themoep.settlements.command;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.utils.Utils;

public class TaxCommand extends ResidentSubCommand {

    public TaxCommand() {
        super(null, "tax info");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new ResidentSubCommand(this, "info") {
            @Override
            public boolean run(Resident sender, String base, String alias, String... args) {
                Resident target = sender;
                if (args.length > 0 && sender.hasPermission("tax.others")) {
                    target = ResidentManager.getInstance().getResident(args[0]);
                    if (target == null) {
                        getError("player-not-found").tell(sender, "name", args[0]);
                        return false;
                    }
                    sender.tell(target.getName() + ":");
                }
                long settlementTax = target.getSettlementTax();
                long plotTax = target.getPlotTax();
                getMessage("info").tell(sender,
                        "state", target.hasSettlement() ? target.getSettlement().getStateTag() : SettlementState.VILLAGE.getTag(),
                        "tax", Utils.CURRENCY_FORMAT.format(settlementTax),
                        "plottax", Utils.CURRENCY_FORMAT.format(plotTax),
                        "plotamount", String.valueOf(LandManager.getInstance().getOwnedPlot(target.getUUID()).size()),
                        "totaltax", Utils.CURRENCY_FORMAT.format(settlementTax + plotTax)
                );
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return false;
    }
}
