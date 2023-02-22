package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.AllyCommand;
import de.themoep.settlements.resident.Resident;

public class EmpireAllyCommand extends EmpireSubCommand implements AllyCommand {
    public EmpireAllyCommand(SubCommand parent) {
        super(parent, "ally s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return addAlly(resident, resident.getSettlement().getEmpire(), args);
    }
}
