package me.crz.minecraft.kingdoms.command;

import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class WarningToggleCommand extends ResidentSubCommand {

    public WarningToggleCommand() {
        super(null, "warningtoggle <settlement>", "togglewarning");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        Village village = VillageManager.getInstance().getVillageFromName(args[0]);
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
