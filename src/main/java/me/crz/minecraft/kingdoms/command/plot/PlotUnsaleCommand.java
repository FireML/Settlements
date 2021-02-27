package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.resident.Resident;

public class PlotUnsaleCommand extends PlotSubCommand {
    public PlotUnsaleCommand(PlotCommand parent) {
        super(parent, "unsale", true);
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (!land.isForSale()) {
            res.tell(getError("not-for-sale"));
        } else {
            land.setForSale(false);
            land.setPrice(0);
            land.setEmbassy(false);
            LandManager.getInstance().saveLand(land);
            res.tell(getMessage("success"));
        }
        return true;
    }
}
