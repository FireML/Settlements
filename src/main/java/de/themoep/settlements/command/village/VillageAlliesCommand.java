package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.AllyCommand;

public class VillageAlliesCommand extends VillageSubCommand implements AllyCommand {
    public VillageAlliesCommand(SubCommand parent) {
        super(parent, "allies");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return listAllies(resident, resident.getVillage());
    }
}
