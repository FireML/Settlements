package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.AllyCommand;
import de.themoep.settlements.resident.Resident;

public class SettlementUnAllyCommand extends SettlementSubCommand implements AllyCommand {
    public SettlementUnAllyCommand(SubCommand parent) {
        super(parent, "unally s:<settlement>|e:<empire>", "rejectally");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        return removeAlly(resident, resident.getSettlement(), args);
    }
}
