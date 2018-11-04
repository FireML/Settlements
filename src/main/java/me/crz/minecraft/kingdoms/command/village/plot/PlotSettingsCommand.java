package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.gui.SettingsGUI;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotSettingsCommand extends ResidentSubCommand {
    public PlotSettingsCommand(SubCommand parent) {
        super(parent, "settings");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            getError("not-a-plot").tell(res);
        } else if (args.length < 1) {
            SettingsGUI.getInstance().open(res.getPlayer());
        }
        return true;
    }
}
