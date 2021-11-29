package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.ResidentPermsSubCommand;
import de.themoep.settlements.command.SubCommand;

public abstract class VillageSubCommand extends ResidentPermsSubCommand {

    public VillageSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (!resident.hasSettlement()) {
            getError("settlement-required").tell(resident);
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasSettlement() && super.hasAccess(resident);
    }

}
