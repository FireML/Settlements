package de.themoep.settlements.command.plot;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlotBuyCommand extends ResidentSubCommand {
    private final Cache<UUID, String> plotBuyTimeout = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).build();

    public PlotBuyCommand(PlotCommand parent) {
        super(parent, "buy");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            getError("not-a-claim").tell(res);
        } else if (!(land.isEmbassy() || land.isOwnedBy(res))) {
            getError("not-your-settlement").tell(res, "state", land.getLandOwner().getStateTag());
        } else if (!land.isForSale() || land.getLandOwner().isAbandoned()) {
            getError("not-for-sale").tell(res);
        } else {
            if (land.isEmbassy() && land.isOwnedBy(res)) {
                getError("cant-buy-embassy").tell(res, "state", res.getSettlement().getState().getTag());
                return true;
            }
            if (land.getTax() > 0) {
                String cached = plotBuyTimeout.getIfPresent(res.getUUID());
                if (cached == null || !cached.equals(land.getKey())) {
                    plotBuyTimeout.put(res.getUUID(), land.getKey());
                    getMessage("tax-warning").tell(res, "tax", Utils.CURRENCY_FORMAT.format(land.getTax()));
                    return true;
                }
                plotBuyTimeout.invalidate(res.getUUID());
            }
            if (land.getPrice() > 0) {
                if (SettlementsPlugin.getInstance().getEconomy().getBalance(res.getPlayer()) < land.getPrice()) {
                    getError("not-enough-money").tell(res, "amount", String.valueOf(land.getPrice()));
                    return true;
                }
                SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(res.getPlayer(), land.getPrice());
                land.getLandOwner().setBank(land.getLandOwner().getBank() + land.getPrice());
                Utils.runAsync(() -> DatabaseManager.getInstance().save(land.getLandOwner()));
            }
            land.setForSale(false);
            getMessage("purchased").tell(res, "price", String.valueOf(land.getPrice()));
            LandManager.getInstance().setPlotOwner(land, res.getUUID());
        }
        return true;
    }
}
