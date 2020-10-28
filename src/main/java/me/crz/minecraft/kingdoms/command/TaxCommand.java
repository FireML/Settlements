package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageState;

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
                long villageTax = target.getVillageTax();
                long plotTax = target.getPlotTax();
                getMessage("info").tell(sender,
                        "state", target.hasVillage() ? target.getVillage().getStateTag() : VillageState.VILLAGE.getTag(),
                        "tax", Utils.CURRENCY_FORMAT.format(villageTax),
                        "plottax", Utils.CURRENCY_FORMAT.format(plotTax),
                        "plotamount", String.valueOf(LandManager.getInstance().getOwnedPlot(target.getUUID()).size()),
                        "totaltax", Utils.CURRENCY_FORMAT.format(villageTax + plotTax)
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
