package de.themoep.settlements.command.plot;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;

public class PlotUnownCommand extends PlotSubCommand {
    public PlotUnownCommand(PlotCommand parent) {
        super(parent, "unown", "revoke");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (!land.isPlot()) {
            getError("not-a-plot").tell(res);
        } else {
            if (land.isPlotOwner(res)) {
                getMessage("success").tell(res);
            } else if (land.getSettlement().canAdminPlots(res)) {
                getMessage("success-other").tell(res, "owner", ResidentManager.getInstance().getResident(land.getPlotOwner()).getName());
            } else {
                getError("cant-admin-plots").tell(res);
                return false;
            }
            LandManager.getInstance().setPlotOwner(land, null);
        }
        return true;
    }
}
