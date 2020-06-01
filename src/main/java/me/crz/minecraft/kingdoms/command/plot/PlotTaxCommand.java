package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.TaxDebtAction;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotTaxCommand extends PlotSubCommand {
    public PlotTaxCommand(SubCommand parent) {
        super(parent, "tax [set <amount>]");
        registerCommand(new PlotSubCommand(this, "set <amount>") {
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
                    int maxTax = Utils.getPlotMaxTax();
                    if (maxTax > -1 && amount > maxTax) {
                        getMessage("above-max").tell(resident,
                                "amount", String.valueOf(amount),
                                "max", String.valueOf(maxTax)
                        );
                        return true;
                    }
                    land.setTax(amount);
                    getMessage("success").tell(resident,
                            "state", land.getLandOwner().getStateTag(),
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
