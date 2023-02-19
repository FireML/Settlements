package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.ResidentPermsSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.config.Config;

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
        if (!resident.getSettlement().hasUnlocked(getPermission()) && Config.Citizens.UNLOCK_DEPENDENCY) {
            resident.tell(getUnlockText(resident.getSettlement(), getPermission()));
            return false;
        }
        if (resident.getSettlement().useGuestRank()
                && resident.getSettlementRank() == resident.getSettlement().getDefaultRank()
                && !resident.getSettlement().getEffectivePermissionValue(resident.getSettlementRank(), getPermission().substring("settlements.command.".length())).orElse(false)) {
            getError("rules-not-accepted").tell(resident, "state", resident.getSettlement().getStateTag(), "name", resident.getSettlement().getName());
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasSettlement() && super.hasAccess(resident);
    }

}
