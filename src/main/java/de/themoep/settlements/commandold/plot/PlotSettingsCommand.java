package de.themoep.settlements.commandold.plot;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.gui.plot.SettingsGUI;
import de.themoep.settlements.resident.Resident;

public class PlotSettingsCommand extends PlotSubCommand {
    public PlotSettingsCommand(SubCommand parent) {
        super(parent, "settings", "gui");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length < 1) {
            new SettingsGUI(res).open();
        }
        return true;
    }
}
