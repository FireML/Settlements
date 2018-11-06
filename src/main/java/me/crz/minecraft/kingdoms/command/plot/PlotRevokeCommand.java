package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotRevokeCommand extends PlotSubCommand {
    public PlotRevokeCommand(PlotCommand parent) {
        super(parent, "revoke", true);
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (!land.isPlot()) {
            res.tell(getError("not-a-plot"));
        } else {
            LandManager.getInstance().setPlotOwner(land, null);
            res.tell(getMessage("success"));
        }
        return true;
    }
}
