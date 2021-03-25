package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.gui.BuildGui;
import me.crz.minecraft.kingdoms.land.BuildState;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.resident.Resident;

public class PlotBuildCommand extends PlotSubCommand {
    public PlotBuildCommand(PlotCommand parent) {
        super(parent, "build <state>");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length < 1) {
            new BuildGui(land).open(res.getPlayer());
        } else if (land.isPlot() && "reset".equalsIgnoreCase(args[0])) {
            land.setBuildState(BuildState.PRIVATE);
            LandManager.getInstance().saveLand(land);
            getMessage("reset").tell(res, "parent", land.getLandOwner().getStateTag(), "state", land.getLandOwner().getBuildState().getTag());
        } else {
            try {
                BuildState newState = BuildState.valueOf(args[0].toUpperCase(), land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getLandOwner()));
                land.setBuildState(newState);
                LandManager.getInstance().saveLand(land);
                getMessage("success").tell(res, "state", newState.getTag());
            } catch (IllegalArgumentException e) {
                Message.get("usage.head").tell(res, "type", "", "name", "Plot Build");
                if (land.isPlot()) {
                    Message.get("usage.entry").tell(res,
                            "usage", getUsage(base).replace("<state>", "reset"),
                            "aliases", "",
                            "path", base + " " + alias + " reset",
                            "description", getMessage("states.reset").getReplacedText(
                                    "parent", land.getLandOwner().getStateTag(),
                                    "state", land.getLandOwner().getBuildState().getTag()
                            )
                    );
                }
                for (BuildState state : BuildState.values(land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getLandOwner()))) {
                    Message.get("usage.entry").tell(res,
                            "usage", getUsage(base).replace("<state>", state.name().toLowerCase()),
                            "aliases", "",
                            "path", base + " " + alias + " " + state.name().toLowerCase(),
                            "description", getMessage("states." + state.getKey()).getText()
                    );
                }
            }
        }
        return true;
    }
}
