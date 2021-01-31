package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.UseFlag;
import me.crz.minecraft.kingdoms.land.UseFlagManager;
import me.crz.minecraft.kingdoms.land.gui.UseGui;
import me.crz.minecraft.kingdoms.land.BuildState;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlotUseCommand extends PlotSubCommand {
    public PlotUseCommand(PlotCommand parent) {
        super(parent, "use <flag> <state>|parent");
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
            new UseGui(land).open(res.getPlayer());
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
