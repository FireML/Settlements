package de.themoep.settlements.command;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.village.SettlementManager;
import de.themoep.settlements.utils.config.Config;

public class WarningToggleCommand extends ResidentSubCommand {

    public WarningToggleCommand() {
        super(null, "warningtoggle <settlement>", "togglewarning");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        Settlement settlement = SettlementManager.getInstance().getFromName(args[0]);
        if (settlement == null) {
            getError("settlement-doesnt-exist").tell(resident, "name", args[0]);
            return true;
        }

        getMessage(resident.toggleCloseBuildingNotification(settlement) ? "enabled" : "disabled").tell(resident,
                "state", settlement.getStateTag(),
                "settlement", settlement.getName(),
                "time", String.valueOf(Config.Settlement.Claiming.Close.Building.TOGGLE_TIME)
        );
        return true;
    }
}
