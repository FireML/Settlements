package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.Base;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotBuyCommand extends VillageSubCommand {
    public PlotBuyCommand(VillagePlotCommand parent) {
        super(parent, "buy", VillageRank.RESIDENT);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null || !res.getVillage().equals(land.getOwnerVillage())) {
            res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
        } else if (!land.isForSale()) {
                res.tell(getError("not-for-sale"));
        } else {
            if (Base.getEconomy().getBalance(res.getPlayer()) < land.getPrice()) {
                res.tell(getError("not-enough-money"), "amount", String.valueOf(land.getPrice()));
                return true;
            }
            Base.getEconomy().withdrawPlayer(res.getPlayer(), (double) land.getPrice());
            land.getOwnerVillage().setBank(land.getOwnerVillage().getBank() + land.getPrice());
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(land.getOwnerVillage()));
            land.setForSale(false);
            res.tell(getMessage("purchased"), "price", String.valueOf(land.getPrice()));
            land.setPrice(0);
            LandManager.getInstance().setPlotOwner(land, res.getUUID());
        }
        return true;
    }
}
