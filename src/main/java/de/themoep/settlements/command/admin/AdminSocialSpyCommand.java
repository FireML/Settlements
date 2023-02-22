package de.themoep.settlements.command.admin;

import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;

public class AdminSocialSpyCommand extends ResidentSubCommand {
    public AdminSocialSpyCommand(SubCommand parent) {
        super(parent, "socialspy", "ss");
        setPermission("settlements.admin.socialspy");
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
