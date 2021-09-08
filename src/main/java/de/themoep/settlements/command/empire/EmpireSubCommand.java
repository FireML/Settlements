package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.village.VillageSubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.MessageUtils;

public abstract class EmpireSubCommand extends VillageSubCommand {

    public EmpireSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (!resident.hasEmpire()) {
            MessageUtils.tell(resident, getError("empire-required"));
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasEmpire() && super.hasAccess(resident);
    }
}
