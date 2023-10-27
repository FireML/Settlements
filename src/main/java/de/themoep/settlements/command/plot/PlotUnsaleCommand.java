package de.themoep.settlements.command.plot;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;

public class PlotUnsaleCommand extends PlotSubCommand {
    public PlotUnsaleCommand(PlotCommand parent) {
        super(parent, "unsale", true);
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (!land.isForSale()) {
            res.tell(getError("not-for-sale").getText());
        } else {
            land.setForSale(false);
            land.setPrice(0);
            land.setEmbassy(false);
            LandManager.getInstance().saveLand(land);
            res.tell(getMessage("success").getText());
        }
        return true;
    }
}
