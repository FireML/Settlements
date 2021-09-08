package de.themoep.settlements.command;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
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

        Village village = VillageManager.getInstance().getFromName(args[0]);
        if (village == null) {
            getError("village-doesnt-exist").tell(resident, "name", args[0]);
            return true;
        }

        getMessage(resident.toggleCloseBuildingNotification(village) ? "enabled" : "disabled").tell(resident,
                "state", village.getStateTag(),
                "settlement", village.getName(),
                "time", String.valueOf(Config.Settlement.Claiming.Close.Building.TOGGLE_TIME)
        );
        return true;
    }
}
