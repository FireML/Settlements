package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotBuildCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotBuyCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotNameCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotLinkCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotSettingsCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotUnlinkCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotInfoCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotModeCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotRevokeCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotSaleCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotUnsaleCommand;
import me.crz.minecraft.kingdoms.command.village.plot.PlotUseCommand;

import me.crz.minecraft.kingdoms.village.Resident;

import me.crz.minecraft.kingdoms.rank.VillageRank;

import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillagePlotCommand extends VillageSubCommand {
    public VillagePlotCommand(SubCommand parent) {
        super(parent, "plot <options>", VillageRank.RESIDENT, "p");
        setPermission("kingdoms.user");
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
