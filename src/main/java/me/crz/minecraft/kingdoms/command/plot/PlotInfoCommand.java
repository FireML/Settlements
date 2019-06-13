package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;

public class PlotInfoCommand extends ResidentSubCommand {
    public PlotInfoCommand(PlotCommand parent) {
        super(parent, "info", "here", "show");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            res.tell(getError("not-a-claim"));
            return true;
        }
        res.tell(getMessage("general"), land.getReplacements());
        if (!land.getLandOwner().isAbandoned()) {
            if (land.isPlot()) {
                Utils.runAsync(() -> res.tell(getMessage("owner"), "name", ResidentManager.getInstance().getResident(land.getPlotOwner()).getName()));
            } else if (land.isForSale() &&
                    (land.isEmbassy() || land.isOwnedBy(res))) {
                res.tell(getMessage("price"), "price", String.valueOf(land.getPrice()));
            }
        }
        return true;
    }
}
