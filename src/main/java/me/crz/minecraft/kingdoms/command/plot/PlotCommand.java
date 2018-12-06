package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotCommand extends ResidentSubCommand {
    public PlotCommand() {
        super(null, "plot <options>");
        setHelpCommand(new HelpCommand(this));
        PlotHelpCommand staffHelpCommand = new PlotHelpCommand(getHelpCommand());
        getHelpCommand().registerCommand(staffHelpCommand);
        registerCommand(new PlotSubCommand(getHelpCommand(), "staff [<page>]", true) {

            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                return staffHelpCommand.run(res, base, alias, args);
            }

            @Override
            public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
                return run(res, base, alias, args);
            }
        });
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
