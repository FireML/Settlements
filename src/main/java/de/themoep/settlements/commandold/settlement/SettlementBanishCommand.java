package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.commandold.common.BanishCommand;
import de.themoep.settlements.resident.Resident;

public class SettlementBanishCommand extends SettlementSubCommand implements BanishCommand {

    public SettlementBanishCommand(SubCommand parent) {
        super(parent, "banish <player>|s:<settlement> <reason>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return banish(resident, resident.getSettlement(), args);
    }
}
