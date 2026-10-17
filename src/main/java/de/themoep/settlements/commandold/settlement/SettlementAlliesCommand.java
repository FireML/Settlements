package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.commandold.common.AllyCommand;
import de.themoep.settlements.resident.Resident;

public class SettlementAlliesCommand extends SettlementSubCommand implements AllyCommand {
    public SettlementAlliesCommand(SubCommand parent) {
        super(parent, "allies");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return listAllies(resident, resident.getSettlement());
    }
}
