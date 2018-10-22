package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.landflags.PlotBuildState;
import me.crz.minecraft.kingdoms.landflags.PlotUseGui;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.BuildState;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlotUseCommand extends VillageSubCommand {
    public PlotUseCommand(VillagePlotCommand parent) {
        super(parent, "use <flag> <state>", VillageRank.RESIDENT);
        registerCommand(new VillageSubCommand(this, "all <state>") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
                if (land == null || !res.getVillage().equals(land.getOwnerVillage())) {
                    res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
                } else if (!land.isPlotOwner(res.getUUID())) {
                    res.tell(getError("not-your-plot"));
                } else {
                    try {
                        PlotBuildState state = PlotBuildState.valueOf(String.join("_", args).toUpperCase());
                        for (Use u : Use.values()) {
                            land.setUseState(u, state);
                        }
                        LandManager.getInstance().syncAndSave(land);
                        res.tell(getMessage("success-all"), "state", state.getTag());
                    } catch (IllegalArgumentException e) {
                        res.tell(getMessage("states"), "states", Arrays.stream(PlotBuildState.values()).map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                        return false;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null || !res.getVillage().equals(land.getOwnerVillage())) {
            res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
        } else if (!land.isPlotOwner(res.getUUID())) {
            res.tell(getError("not-your-plot"));
        } else if (args.length < 2) {
            PlotUseGui.getInstance().open(res.getPlayer());
        } else {
            try {
                Use u = Use.valueOf(args[0].toUpperCase());
                PlotBuildState state = PlotBuildState.valueOf(Arrays.stream(args).skip(1).collect(Collectors.joining("_")).toUpperCase());
                land.setUseState(u, state);
                LandManager.getInstance().syncAndSave(land);
                res.tell(getMessage("success"), "flag", u.name(), "state", state.getTag());
            } catch (IllegalArgumentException e) {
                res.tell(getMessage("flags"), "flags", Arrays.stream(Use.values()).map(u -> u.name().toLowerCase()).collect(Collectors.joining(", ")));
                res.tell(getMessage("states"), "states", Arrays.stream(PlotBuildState.values()).map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                return false;
            }
        }
        return true;
    }
}
