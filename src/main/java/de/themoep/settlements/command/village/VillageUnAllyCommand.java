package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.AllyCommand;

public class VillageUnAllyCommand extends VillageSubCommand implements AllyCommand {
    public VillageUnAllyCommand(SubCommand parent) {
        super(parent, "unally s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        return removeAlly(resident, resident.getVillage(), args);
    }
}
