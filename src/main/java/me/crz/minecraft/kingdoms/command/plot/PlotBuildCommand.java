package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.gui.BuildGui;
import me.crz.minecraft.kingdoms.land.BuildState;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotBuildCommand extends PlotSubCommand {
    public PlotBuildCommand(PlotCommand parent) {
        super(parent, "build <state>");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length < 1) {
            new BuildGui(land).open(res.getPlayer());
        } else {
            try {
                BuildState newState = BuildState.valueOf(args[0].toUpperCase(), land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getLandOwner()));
                land.setBuildState(newState);
                LandManager.getInstance().saveLand(land);
                res.tell(getMessage("success"), "state", newState.getTag());
            } catch (IllegalArgumentException e) {
                res.tell(Message.get("usage.head"), "name", "Plot Build");
                for (BuildState state : BuildState.values(land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getLandOwner()))) {
                    res.tell(Message.get("usage.entry"),
                            "usage", getUsage(base).replace("<state>", state.name().toLowerCase()),
                            "description", getMessage("states." + state.getKey()).getText()
                    );
                }
            }
        }
        return true;
    }
}
