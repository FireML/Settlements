package de.themoep.settlements.command.plot;

import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.gui.BuildGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.world.SimpleChunkLocation;

public class PlotBuildCommand extends PlotSubCommand {
    public PlotBuildCommand(PlotCommand parent) {
        super(parent, "build <state>");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length < 1) {
            SimpleChunkLocation landLocation = land.getLocation();
            new BuildGUI(
                    "plot",
                    land,
                    () -> {
                        if (!res.hasPermission(getPermission())) {
                            return false;
                        }

                        OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                        if (currentLand != null) {
                            return currentLand.isPlotOwner(res) || currentLand.canAdmin(res);
                        }
                        return false;
                    }
            ).open(res.getPlayer());
        } else if (land.isPlot() && "reset".equalsIgnoreCase(args[0])) {
            land.setBuildState(null);
            LandManager.getInstance().saveLand(land);
            getMessage("reset").tell(res, "parent", land.getSettlement().getStateTag(), "state", land.getSettlement().getBuildState().getTag());
        } else {
            try {
                BuildState newState = BuildState.valueOf(args[0].toUpperCase(), land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getSettlement()));
                land.setBuildState(newState);
                LandManager.getInstance().saveLand(land);
                getMessage("success").tell(res, "state", newState.getTag(), "type", land.getBuildTag());
            } catch (IllegalArgumentException e) {
                Message.get("usage.head").tell(res, "type", "", "name", "Plot Build");
                if (land.isPlot()) {
                    Message.get("usage.entry").tell(res,
                            "usage", getUsage(base).replace("<state>", "reset"),
                            "aliases", "",
                            "path", base + " " + alias + " reset",
                            "description", getMessage("states.reset").getReplacedText(
                                    "parent", land.getSettlement().getStateTag(),
                                    "state", land.getSettlement().getBuildState().getTag()
                            )
                    );
                }
                for (BuildState state : BuildState.values(land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getSettlement()))) {
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
