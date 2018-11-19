package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotCommand extends ResidentSubCommand {
    public PlotCommand() {
        super(null, "plot <options>", "p");
        setPermission("kingdoms.user");
        PlotHelpCommand staffHelpCommand = new PlotHelpCommand(getHelpCommand());
        getHelpCommand().registerCommand(staffHelpCommand);
        registerCommand(staffHelpCommand);
        registerCommand(new PlotSaleCommand(this));
        registerCommand(new PlotUnsaleCommand(this));
        registerCommand(new PlotInfoCommand(this));
        registerCommand(new PlotNameCommand(this));
        registerCommand(new PlotBuyCommand(this));
        registerCommand(new PlotRevokeCommand(this));
        registerCommand(new PlotSettingsCommand(this));
        registerCommand(new PlotUseCommand(this));
        registerCommand(new PlotModeCommand(this));
        registerCommand(new PlotBuildCommand(this));
        registerCommand(new PlotLinkCommand(this));
        registerCommand(new PlotUnlinkCommand(this));
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        return false;
    }
}
