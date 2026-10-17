package de.themoep.settlements.commandold.plot;

import de.themoep.settlements.commandold.HelpCommand;
import de.themoep.settlements.commandold.ResidentSubCommand;
import de.themoep.settlements.resident.Resident;

public class PlotCommand extends ResidentSubCommand {
    public PlotCommand() {
        super(null, "plot <options>");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new PlotSaleCommand(this));
        registerCommand(new PlotUnsaleCommand(this));
        registerCommand(new PlotInfoCommand(this));
        registerCommand(new PlotNameCommand(this));
        registerCommand(new PlotDescriptionCommand(this));
        registerCommand(new PlotBuyCommand(this));
        registerCommand(new PlotUnownCommand(this));
        registerCommand(new PlotSettingsCommand(this));
        registerCommand(new PlotMembersCommand(this));
        registerCommand(new PlotPermsCommand(this));
        registerCommand(new PlotModeCommand(this));
        registerCommand(new PlotBuildCommand(this));
        registerCommand(new PlotLinkCommand(this));
        registerCommand(new PlotUnlinkCommand(this));
        registerCommand(new PlotTaxCommand(this));
        registerCommand(new PlotResetCommand(this));
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
