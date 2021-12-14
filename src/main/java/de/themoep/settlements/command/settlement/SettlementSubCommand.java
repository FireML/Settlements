package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.ResidentPermsSubCommand;
import de.themoep.settlements.command.SubCommand;

public abstract class SettlementSubCommand extends ResidentPermsSubCommand {

    public SettlementSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (!resident.hasSettlement()) {
            getError("settlement-required").tell(resident);
            return false;
        }
        if (!resident.getSettlement().hasUnlocked(getPermission())) {
            getError("function-not-unlocked").tell(resident);
            return true;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasSettlement() && super.hasAccess(resident);
    }

}
