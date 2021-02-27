package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;

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
            } else if (land.getLandOwner().canAdminPlots(res)) {
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
