package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.BanishCommand;

public class VillageUnbanishCommand extends VillageSubCommand implements BanishCommand {

    public VillageUnbanishCommand(SubCommand parent) {
        super(parent, "unbanish <player>|s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return unbanish(resident, resident.getVillage(), args);
    }
}
