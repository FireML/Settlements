package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.AllyCommand;

public class SettlementAlliesCommand extends SettlementSubCommand implements AllyCommand {
    public SettlementAlliesCommand(SubCommand parent) {
        super(parent, "allies");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return listAllies(resident, resident.getSettlement());
    }
}
