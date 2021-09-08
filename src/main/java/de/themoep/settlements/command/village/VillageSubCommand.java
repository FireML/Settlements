package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.ResidentPermsSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.MessageUtils;

public abstract class VillageSubCommand extends ResidentPermsSubCommand {

    public VillageSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (!resident.hasVillage()) {
            MessageUtils.tell(resident, getError("village-required"));
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasVillage() && super.hasAccess(resident);
    }

}
