package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.BanishCommand;

public class SettlementBanishCommand extends SettlementSubCommand implements BanishCommand {

    public SettlementBanishCommand(SubCommand parent) {
        super(parent, "banish <player>|s:<settlement>|e:<empire> <reason>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return banish(resident, resident.getSettlement(), args);
    }
}
