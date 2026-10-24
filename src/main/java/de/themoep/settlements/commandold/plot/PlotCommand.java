package de.themoep.settlements.commandold.plot;

import de.themoep.settlements.commandold.HelpCommand;
import de.themoep.settlements.commandold.ResidentSubCommand;
import de.themoep.settlements.resident.Resident;

public class PlotCommand extends ResidentSubCommand {
    public PlotCommand() {
        super(null, "plot <options>");
        registerCommand(new PlotInfoCommand(this));
        registerCommand(new PlotMembersCommand(this));
        registerCommand(new PlotBuildCommand(this));
        registerCommand(new PlotLinkCommand(this));
        registerCommand(new PlotUnlinkCommand(this));
    }

    @Override
    public String getKey() {
        return name();
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        return false;
    }
}
