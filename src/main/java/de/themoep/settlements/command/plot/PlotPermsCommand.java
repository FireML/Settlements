package de.themoep.settlements.command.plot;

import de.themoep.settlements.land.*;
import de.themoep.settlements.land.gui.BuildGUI;
import de.themoep.settlements.land.gui.PermsGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.NameTag;
import de.themoep.settlements.world.SimpleChunkLocation;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PlotPermsCommand extends PlotSubCommand {
    public PlotPermsCommand(PlotCommand parent) {
        super(parent, "perms <flag> <state>|parent", "permissions", "rights", "use", "build");
        setForceOwnUsage(true);
        registerCommand(new PlotSubCommand(this, "all <state>") {
            @Override
            public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
                try {
                    BuildState state = BuildState.valueOf(String.join("_", args).toUpperCase(), BuildState.AppliesTo.LAND);
                    for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
                        land.setUseState(u, state);
                    }
                    LandManager.getInstance().syncAndSave(land);
                    getMessage("success-all").tell(res, "state", state.getTag());
                } catch (IllegalArgumentException e) {
                    if (("reset".equalsIgnoreCase(args[1]) || "unset".equalsIgnoreCase(args[1]) || "parent".equalsIgnoreCase(args[1]))) {
                        for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
                            land.unsetUseState(u);
                        }
                        LandManager.getInstance().syncAndSave(land);
                        getMessage("unset-all").tell(res, "parent", land.getLandOwner().getStateTag());
                    } else {
                        getMessage("states").tell(res, "states", BuildState.values(BuildState.AppliesTo.LAND).stream().map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                        return false;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length < 2) {
            SimpleChunkLocation landLocation = land.getLocation();
            Supplier<Boolean> permCheck = () -> {
                if (!res.hasPermission(getPermission())) {
                    return false;
                }

                OwnedLand currentLand = LandManager.getInstance().getLand(landLocation);
                if (currentLand != null) {
                    return currentLand.isPlotOwner(res) || currentLand.canAdmin(res);
                }
                return false;
            };
            new PermsGUI(
                    "plot",
                    land,
                    permCheck,
                    (flagAble, click) -> new BuildGUI("plot", flagAble, permCheck).open(click.getWhoClicked())
            ).open(res.getPlayer());
        } else {
            UseFlag use = UseFlagManager.getInstance().get(args[0]);
            if (use != null) {
                try {
                    BuildState state = BuildState.valueOf(Arrays.stream(args).skip(1).collect(Collectors.joining("_")).toUpperCase(), land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getLandOwner()));
                    land.setUseState(use, state);
                    LandManager.getInstance().syncAndSave(land);
                    getMessage("success").tell(res, "flag", use.name(), "state", state.getTag());
                } catch (IllegalArgumentException e) {
                    if ("reset".equalsIgnoreCase(args[1]) || "unset".equalsIgnoreCase(args[1]) || "parent".equalsIgnoreCase(args[1])) {
                        land.unsetUseState(use);
                        LandManager.getInstance().syncAndSave(land);
                        getMessage("unset-all").tell(res, "parent", land.getLandOwner().getStateTag());
                    } else {
                        getMessage("flags").tell(res, "flags", UseFlagManager.getInstance().getUseFlags().stream().map(u -> u.name().toLowerCase()).collect(Collectors.joining(", ")));
                        return false;
                    }
                }
            } else {
                res.tell(getMessage("states"), "states", BuildState.values(land.isPlot() ? BuildState.AppliesTo.LAND : BuildState.AppliesTo.get(land.getLandOwner())).stream().map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                return false;
            }
        }
        return true;
    }
}
