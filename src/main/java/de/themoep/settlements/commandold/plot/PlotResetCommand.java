package de.themoep.settlements.commandold.plot;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.resident.Resident;

public class PlotResetCommand extends PlotSubCommand {

    public PlotResetCommand(SubCommand parent) {
        super(parent, "reset");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        boolean canAdmin = land.canAdmin(res);
        if (!land.isPlotOwner(res) && !canAdmin) {
            getError("not-your-plot").tell(res, "state", res.getSettlement().getState().getTag());
            return true;
        }
        for (PlotMode mode : PlotMode.values()) {
            land.unsetMode(mode);
        }
        for (UseFlag use : UseFlagManager.getInstance().getUseFlags()) {
            land.unsetUseState(use);
        }
        getMessage("success").tell(res);
        return true;
    }
}
