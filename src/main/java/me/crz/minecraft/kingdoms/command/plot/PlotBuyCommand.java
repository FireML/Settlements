package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotBuyCommand extends ResidentSubCommand {
    public PlotBuyCommand(PlotCommand parent) {
        super(parent, "buy");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            getError("not-a-plot").tell(res);
        } else if (!(land.isEmbassy() || land.isOwnedBy(res))) {
            getError("not-your-village").tell(res, "state", land.getLandOwner().getStateTag());
        } else if (!land.isForSale() || land.getLandOwner().isAbandoned()) {
            getError("not-for-sale").tell(res);
        } else {
            if (land.isEmbassy() && land.isOwnedBy(res)) {
                getError("cant-buy-embassy").tell(res, "state", res.getVillage().getState().getTag());
                return true;
            }
            if (land.getPrice() > 0) {
                if (Base.getEconomy().getBalance(res.getPlayer()) < land.getPrice()) {
                    res.tell(getError("not-enough-money"), "amount", String.valueOf(land.getPrice()));
                    return true;
                }
                Base.getEconomy().withdrawPlayer(res.getPlayer(), (double) land.getPrice());
                land.getLandOwner().setBank(land.getLandOwner().getBank() + land.getPrice());
                Utils.runAsync(() -> DatabaseManager.getInstance().save(land.getLandOwner()));
            }
            land.setForSale(false);
            res.tell(getMessage("purchased"), "price", String.valueOf(land.getPrice()));
            LandManager.getInstance().setPlotOwner(land, res.getUUID());
        }
        return true;
    }
}
