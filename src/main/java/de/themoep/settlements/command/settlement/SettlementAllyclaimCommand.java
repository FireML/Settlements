package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.Ally;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

public class SettlementAllyclaimCommand extends SettlementSubCommand {
    public SettlementAllyclaimCommand(SubCommand parent) {
        super(parent, "allyclaim <allow|deny>", "setallyclaim");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        boolean value;
        if (args.length > 0) {
            try {
                value = Utils.parseBoolean(args[0]);
            } catch (IllegalArgumentException e) {
                return false;
            }
        } else {
            value = !resident.getSettlement().isAllowAllyClaim();
        }

        resident.getSettlement().setAllowAllyClaim(value);
        if (!value) {
            for (Ally ally : resident.getSettlement().getAllies()) {
                LandManager.getInstance().checkClaimDistance(resident.getSettlement(), ally.getLandOwner());
            }
        }
        DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
        getMessage(value ? "enabled" : "disabled").tell(resident, "grace", String.valueOf(Config.Claiming.Close.Unclaim.GRACE_DAYS));

        return true;
    }
}
