package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.BanishCommand;
import de.themoep.settlements.resident.Resident;

public class SettlementBanishedCommand extends SettlementSubCommand implements BanishCommand {
    public SettlementBanishedCommand(SubCommand parent) {
        super(parent, "banished", "banishlist", "listbanished");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return listBanished(resident, resident.getSettlement());
    }

}
