package de.themoep.settlements.commandold;

import de.themoep.settlements.resident.Resident;

public abstract class ResidentPermsSubCommand extends ResidentSubCommand {

    public ResidentPermsSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasPermission(getPermission());
    }
}
