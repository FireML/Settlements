package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotHelpCommand extends PlotSubCommand {
    public PlotHelpCommand(HelpCommand parent) {
        super(parent, "staff [<page>]", true);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        return getParent().run(res.getPlayer(), base.substring(0, base.lastIndexOf(' ')), base.substring(base.lastIndexOf(' ')), (name() + " " + String.join(" ", args)).split(" "));
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        return run(res, base, alias, args);
    }
}
