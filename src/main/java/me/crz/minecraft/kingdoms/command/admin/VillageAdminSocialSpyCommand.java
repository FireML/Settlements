package me.crz.minecraft.kingdoms.command.admin;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;

public class VillageAdminSocialSpyCommand extends ResidentSubCommand {
    public VillageAdminSocialSpyCommand(SubCommand parent) {
        super(parent, "socialspy", "ss");
        setPermission("kingdoms.admin.socialspy");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        resident.setSocialSpy(!resident.getSocialSpy());
        if (resident.getSocialSpy()) {
            getMessage("enabled").tell(resident);
        } else {
            getMessage("disabled").tell(resident);
        }
        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(resident));
        return true;
    }
}
