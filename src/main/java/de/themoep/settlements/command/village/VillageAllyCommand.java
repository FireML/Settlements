package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.AllyCommand;

public class VillageAllyCommand extends VillageSubCommand implements AllyCommand {
    public VillageAllyCommand(SubCommand parent) {
        super(parent, "ally s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return addAlly(resident, resident.getVillage(), args);
    }
}
