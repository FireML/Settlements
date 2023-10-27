package de.themoep.settlements.command.plot;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;

public class PlotSaleCommand extends PlotSubCommand {
    public PlotSaleCommand(PlotCommand parent) {
        super(parent, "sale false|true|<amount>", true);
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (land.isPlot()) {
            res.tell(getMessage("already-owned").getText());
        } else if (args.length < 1) {
            if (land.isForSale()) {
                res.tell(getMessage("current").getText(), "price", String.valueOf(land.getPrice()));
            } else {
                return false;
            }
        } else if ("false".equalsIgnoreCase(args[0]) || "no".equalsIgnoreCase(args[0])) {
            land.setForSale(false);
            land.setEmbassy(false);
            LandManager.getInstance().saveLand(land);
            res.tell(getMessage("no-longer-for-sale").getText());
        } else if ("true".equalsIgnoreCase(args[0]) || "yes".equalsIgnoreCase(args[0])) {
            land.setForSale(true);
            LandManager.getInstance().saveLand(land);
            res.tell(getMessage("success").getText(), "price", String.valueOf(land.getPrice()));
        } else {
            try {
                int price = Integer.parseInt(args[0]);
                if (price < 0) {
                    getError("not-a-number").tell(res, "input", args[0]);
                    return true;
                }
                land.setPrice(price);
                land.setForSale(true);
                LandManager.getInstance().saveLand(land);
                res.tell(getMessage("success").getText(), "price", String.valueOf(price));
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(res, "input", args[0]);
                return false;
            }
        }
        return true;
    }
}
