package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotSaleCommand extends VillageSubCommand {
    public PlotSaleCommand(VillagePlotCommand parent) {
        super(parent, "sale false|true|<amount>", VillageRank.ELDER);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
        } else if (!res.getVillage().equals(land.getOwnerVillage())) {
            res.tell(getError("not-your-village"), "state", land.getOwnerVillage().getState().getTag());
        } else if (land.isPlot()) {
            res.tell(getMessage("already-owned"));
        } else if (args.length < 1) {
            if (land.isForSale()) {
                res.tell(getMessage("current"), "price", String.valueOf(land.getPrice()));
            } else {
                return false;
            }
        } else if ("false".equalsIgnoreCase(args[0]) || "no".equalsIgnoreCase(args[0])) {
            land.setForSale(false);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
            res.tell(getMessage("no-longer-for-sale"));
        } else if ("true".equalsIgnoreCase(args[0]) || "yes".equalsIgnoreCase(args[0])) {
            land.setForSale(true);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
            res.tell(getMessage("success"), "price", String.valueOf(land.getPrice()));
        } else {
            try {
                int price = Integer.valueOf(args[0]);
                land.setPrice(price);
                land.setForSale(true);
                Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
                res.tell(getMessage("success"), "price", String.valueOf(price));
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
