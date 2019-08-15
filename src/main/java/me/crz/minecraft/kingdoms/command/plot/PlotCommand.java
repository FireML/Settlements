package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotCommand extends ResidentSubCommand {
    public PlotCommand(SubCommand parent) {
        super(parent, "plot <options>");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new PlotSaleCommand(this));
        registerCommand(new PlotUnsaleCommand(this));
        registerCommand(new PlotInfoCommand(this));
        registerCommand(new PlotNameCommand(this));
        registerCommand(new PlotBuyCommand(this));
        registerCommand(new PlotUnownCommand(this));
        registerCommand(new PlotSettingsCommand(this));
        registerCommand(new PlotUseCommand(this));
        registerCommand(new PlotModeCommand(this));
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
