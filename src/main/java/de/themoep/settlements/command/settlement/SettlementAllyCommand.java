package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.AllyCommand;
import de.themoep.settlements.resident.Resident;

public class SettlementAllyCommand extends SettlementSubCommand implements AllyCommand {
    public SettlementAllyCommand(SubCommand parent) {
        super(parent, "ally s:<settlement>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return addAlly(resident, resident.getSettlement(), args);
    }
}
