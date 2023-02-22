package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;

public abstract class EmpireSubCommand extends ResidentSubCommand {

    public EmpireSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (!resident.hasEmpire()) {
            getError("empire-required").tell(resident);
            return false;
        }
        if (!resident.getSettlement().getEmpire().hasUnlocked(getPermission())) {
            resident.tell(getUnlockText(resident.getSettlement().getEmpire(), getPermission()));
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasEmpire() && super.hasAccess(resident);
    }
}
