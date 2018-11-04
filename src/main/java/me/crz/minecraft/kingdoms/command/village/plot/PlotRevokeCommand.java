package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotRevokeCommand extends VillageSubCommand {
    public PlotRevokeCommand(VillagePlotCommand parent) {
        super(parent, "revoke", VillageRank.ELDER);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            res.tell(getError("not-a-plot"));
        } else if (!land.isOwnedBy(res.getVillage())) {
            res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
        } else if (!land.isPlot()) {
            res.tell(getError("not-a-plot"));
        } else {
            LandManager.getInstance().setPlotOwner(land, null);
            res.tell(getMessage("success"));
        }
        return true;
    }
}
