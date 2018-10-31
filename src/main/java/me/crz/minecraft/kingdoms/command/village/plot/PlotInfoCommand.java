package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;

public class PlotInfoCommand extends VillageSubCommand {
    public PlotInfoCommand(VillagePlotCommand parent) {
        super(parent, "info", VillageRank.RESIDENT, "here", "show");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            res.tell(getError("not-a-plot"));
            return true;
        }
        res.tell(getMessage("general"), land.getReplacements(this));
        if (land.isPlot()) {
            Utils.runAsync(() -> res.tell(getMessage("owner"), "name", ResidentManager.getInstance().getResident(land.getPlotOwner()).getName()));
        } else if (land.isForSale() &&
                (land.isEmbassy() || land.isOwnedBy(res.getVillage()))) {
            res.tell(getMessage("price"), "price", String.valueOf(land.getPrice()));
        }
        return true;
    }
}
