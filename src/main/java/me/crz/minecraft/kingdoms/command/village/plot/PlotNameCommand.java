package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotNameCommand extends ResidentSubCommand {
    public PlotNameCommand(VillagePlotCommand parent) {
        super(parent, "name <text>", "", "board", "description", "setdescription");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            res.tell(getError("not-a-plot"));
            return true;
        }
        if (!land.isPlotOwner(res) && !(res.getVillageRank().atLeast(VillageRank.ELDER) && land.isOwnedBy(res.getVillage()))) {
            res.tell(getError("not-your-plot"));
            return true;
        }
        land.setDescription(String.join(" ", args));
        Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
        getMessage("success").tell(res, "description", land.getDescription());
        return true;
    }
}
