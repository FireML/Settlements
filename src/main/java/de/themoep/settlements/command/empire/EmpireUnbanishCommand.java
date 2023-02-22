package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.BanishCommand;
import de.themoep.settlements.resident.Resident;

public class EmpireUnbanishCommand extends EmpireSubCommand implements BanishCommand {

    public EmpireUnbanishCommand(SubCommand parent) {
        super(parent, "unbanish <player>|s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return unbanish(resident, resident.getSettlement().getEmpire(), args);
    }
}
