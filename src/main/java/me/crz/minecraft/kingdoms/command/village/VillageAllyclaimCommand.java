package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.resident.Resident;

public class VillageAllyclaimCommand extends VillageSubCommand {
    public VillageAllyclaimCommand(SubCommand parent) {
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
            value = !resident.getVillage().isAllowAllyClaim();
        }

        resident.getVillage().setAllowAllyClaim(value);
        if (!value) {
            for (LandOwner ally : resident.getVillage().getAllies()) {
                LandManager.getInstance().checkClaimDistance(resident.getVillage(), ally);
            }
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
        getMessage(value ? "enabled" : "disabled").tell(resident, "grace", String.valueOf(Config.Settlement.Claiming.Close.Unclaim.GRACE_DAYS));

        return true;
    }
}
