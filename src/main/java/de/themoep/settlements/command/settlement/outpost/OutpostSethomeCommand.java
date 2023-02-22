package de.themoep.settlements.command.settlement.outpost;

import de.themoep.settlements.command.settlement.SettlementSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;

import java.util.Collection;

public class OutpostSethomeCommand extends SettlementSubCommand {
    public OutpostSethomeCommand(SettlementSubCommand parent) {
        super(parent, "sethome");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        OwnedLand currentLand = LandManager.getInstance().getLand(resident.getPlayer().getLocation());
        if (currentLand == null || !currentLand.isOwnedBy(resident.getSettlement())) {
            resident.tell(getError("not-an-outpost"));
            return true;
        }
        OwnedLand outpostLand = null;
        if (currentLand.isOutpost()) {
            outpostLand = currentLand;
        } else {
            Collection<OwnedLand> connected = LandManager.getInstance().getAllConnectedLand(currentLand, search -> {
                if (search.isOutpost()) {
                    return LandManager.FilterResponse.RETURN;
                } else if (search.containsHome()) {
                    return LandManager.FilterResponse.SKIP_ENTRY;
                }
                return LandManager.FilterResponse.CONTINUE;
            });
            for (OwnedLand l : connected) {
                if (l.isOutpost()) {
                    outpostLand = l;
                    break;
                }
            }
        }
        if (outpostLand == null) {
            resident.tell(getError("not-an-outpost"));
            return true;
        }
        resident.getSettlement().setOutpostHome(outpostLand.getOutpostName(), resident.getPlayer().getLocation());
        if (outpostLand != currentLand) {
            currentLand.setOutpost(outpostLand.getOutpostName());
            outpostLand.setOutpost(null);
        }
        OwnedLand finalOutpostLand = outpostLand;
        DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
        DatabaseManager.getInstance().saveLand(currentLand);
        if (finalOutpostLand != currentLand) {
            DatabaseManager.getInstance().saveLand(finalOutpostLand);
        }
        resident.tell(getMessage("success"), "name", currentLand.getOutpostName());
        return true;
    }
}
