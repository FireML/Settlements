package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.common.AllyCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;

public class EmpireAlliesCommand extends EmpireSubCommand implements AllyCommand {
    public EmpireAlliesCommand(SubCommand parent) {
        super(parent, "allies");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return listAllies(resident, resident.getVillage().getEmpire());
    }
}
