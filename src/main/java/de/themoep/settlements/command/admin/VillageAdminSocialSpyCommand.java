package de.themoep.settlements.command.admin;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;

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
        DatabaseManager.getInstance().saveResident(resident);
        return true;
    }
}
