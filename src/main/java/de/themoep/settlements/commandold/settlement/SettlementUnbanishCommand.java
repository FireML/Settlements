package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.commandold.common.BanishCommand;
import de.themoep.settlements.resident.Resident;

public class SettlementUnbanishCommand extends SettlementSubCommand implements BanishCommand {

    public SettlementUnbanishCommand(SubCommand parent) {
        super(parent, "unbanish <player>|s:<settlement>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return unbanish(resident, resident.getSettlement(), args);
    }
}
