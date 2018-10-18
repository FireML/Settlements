package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.landflags.PlotBuildGui;
import me.crz.minecraft.kingdoms.landflags.PlotBuildState;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotBuildCommand extends VillageSubCommand {
    public PlotBuildCommand(VillagePlotCommand parent) {
        super(parent, "build <state>", VillageRank.RESIDENT);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null || !res.getVillage().equals(land.getOwnerVillage())) {
            res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
        } else if (!land.isPlotOwner(res.getUUID())) {
            res.tell(getError("not-your-plot"));
        } else if (args.length < 1) {
            new PlotBuildGui().open(res.getPlayer());
        } else {
            try {
                PlotBuildState newState = PlotBuildState.valueOf(args[0].toUpperCase());
                land.setBuildState(newState);
                Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
                res.tell(getMessage("success"), "state", newState.getTag());
            } catch (IllegalArgumentException e) {
                res.tell(Message.get("usage.head"), "name", "Plot Build");
                for (PlotBuildState state : PlotBuildState.values()) {
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
