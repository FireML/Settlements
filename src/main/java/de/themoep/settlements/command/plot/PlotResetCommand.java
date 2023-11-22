package de.themoep.settlements.command.plot;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.*;
import de.themoep.settlements.land.gui.plot.ModeGUI;
import de.themoep.settlements.land.gui.plot.SettingsGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;

import java.util.Arrays;
import java.util.stream.Collectors;

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
