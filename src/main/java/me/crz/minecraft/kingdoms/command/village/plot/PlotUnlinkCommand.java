package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotUnlinkCommand extends ResidentSubCommand {
    public PlotUnlinkCommand(VillagePlotCommand parent) {
        super(parent, "unlink", "disconnect");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            getError("not-a-plot").tell(res);
        } else if (!(land.isPlotOwner(res.getUUID()) || (land.isOwnedBy(res.getVillage()) && res.getVillageRank().atLeast(VillageRank.ELDER)))) {
            getError("not-your-plot").tell(res);
        } else if (!land.hasLink()) {
            getError("none-linked").tell(res);
        } else {
            int linked = land.getLinked().size() - 1;
            getMessage("disconnected").tell(res, "amount", String.valueOf(linked));
            if (land.removeLink()) {
                Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
            }
        }
        return true;
    }
}
