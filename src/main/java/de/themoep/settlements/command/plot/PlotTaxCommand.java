package de.themoep.settlements.command.plot;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

public class PlotTaxCommand extends PlotSubCommand {
    public PlotTaxCommand(SubCommand parent) {
        super(parent, "tax [set <amount>]");
        registerCommand(new PlotSubCommand(this, "set <amount>", true) {
            @Override
            public boolean run(Resident resident, OwnedLand land, String base, String alias, String... args) {
                if (args.length == 0) {
                    return false;
                } else if (land.isPlot()) {
                    getMessage("already-owned").tell(resident, land.getReplacements());
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[0]);
                    if (amount < 0) {
                        getMessage("below-zero").tell(resident,
                                "amount", String.valueOf(amount)
                        );
                        return true;
                    }
                    if (Config.Plot.MAX_TAX > -1 && amount > Config.Plot.MAX_TAX) {
                        getMessage("above-max").tell(resident,
                                "amount", String.valueOf(amount),
                                "max", String.valueOf(Config.Plot.MAX_TAX)
                        );
                        return true;
                    }
                    land.setTax(amount);
                    getMessage("success").tell(resident,
                            "state", land.getSettlement().getStateTag(),
                            "amount", String.valueOf(amount)
                    );
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(resident, "input", args[0]);
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, OwnedLand land, String base, String alias, String... args) {
        if (args.length == 0) {
            getMessage("current").tell(resident, land.getReplacements());
            return true;
        }
        return false;
    }
}
