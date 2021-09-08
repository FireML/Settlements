package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.BanishCommand;

public class VillageBanishedCommand extends VillageSubCommand implements BanishCommand {
    public VillageBanishedCommand(SubCommand parent) {
        super(parent, "banished", "banishlist", "listbanished");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return listBanished(resident, resident.getVillage());
    }

}
