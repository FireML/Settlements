package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.gui.SettingsGUI;
import me.crz.minecraft.kingdoms.resident.Resident;

public class PlotSettingsCommand extends PlotSubCommand {
    public PlotSettingsCommand(SubCommand parent) {
        super(parent, "settings", "gui");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length < 1) {
            new SettingsGUI(res.getPlayer());
        }
        return true;
    }
}
