package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.gui.UseGui;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlotUseCommand extends PlotSubCommand {
    public PlotUseCommand(PlotCommand parent) {
        super(parent, "use <flag> <state>");
        setForceOwnUsage(true);
        registerCommand(new PlotSubCommand(this, "all <state>") {
            @Override
            public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
                try {
                    BuildState state = BuildState.valueOf(String.join("_", args).toUpperCase(), BuildState.AppliesTo.LAND);
                    for (Use u : Use.values()) {
                        land.setUseState(u, state);
                    }
                    LandManager.getInstance().syncAndSave(land);
                    res.tell(getMessage("success-all"), "state", state.getTag());
                } catch (IllegalArgumentException e) {
                    res.tell(getMessage("states"), "states", BuildState.values(BuildState.AppliesTo.LAND).stream().map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length < 2) {
            UseGui.getInstance().open(res.getPlayer());
        } else {
            try {
                Use u = Use.valueOf(args[0].toUpperCase());
                BuildState state = BuildState.valueOf(Arrays.stream(args).skip(1).collect(Collectors.joining("_")).toUpperCase(), BuildState.AppliesTo.LAND);
                land.setUseState(u, state);
                LandManager.getInstance().syncAndSave(land);
                res.tell(getMessage("success"), "flag", u.name(), "state", state.getTag());
            } catch (IllegalArgumentException e) {
                res.tell(getMessage("flags"), "flags", Arrays.stream(Use.values()).map(u -> u.name().toLowerCase()).collect(Collectors.joining(", ")));
                res.tell(getMessage("states"), "states", BuildState.values(BuildState.AppliesTo.LAND).stream().map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                return false;
            }
        }
        return true;
    }
}
