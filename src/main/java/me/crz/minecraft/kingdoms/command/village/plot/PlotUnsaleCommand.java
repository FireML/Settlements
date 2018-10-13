package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotUnsaleCommand extends VillageSubCommand {
    public PlotUnsaleCommand(VillagePlotCommand parent) {
        super(parent, "unsale", VillageRank.ELDER);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null || !res.getVillage().equals(land.getOwnerVillage())) {
            res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
        } else if (!land.isForSale()) {
            res.tell(getError("not-for-sale"));
        } else {
            land.setForSale(false);
            land.setPrice(0);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
            res.tell(getMessage("success"));
        }
        return true;
    }
}
