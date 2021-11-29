package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.BanishCommand;

public class EmpireBanishedCommand extends EmpireSubCommand implements BanishCommand {
    public EmpireBanishedCommand(SubCommand parent) {
        super(parent, "banished", "banishlist", "listbanished");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return listBanished(resident, resident.getSettlement().getEmpire());
    }
}
