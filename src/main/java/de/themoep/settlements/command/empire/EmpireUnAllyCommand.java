package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.AllyCommand;
import de.themoep.settlements.resident.Resident;

public class EmpireUnAllyCommand extends EmpireSubCommand implements AllyCommand {
    public EmpireUnAllyCommand(SubCommand parent) {
        super(parent, "unally s:<settlement>|e:<empire>", "rejectally");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return removeAlly(resident, resident.getSettlement().getEmpire(), args);
    }
}
