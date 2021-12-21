package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.LandOwnerCommand;
import de.themoep.settlements.resident.Resident;

public class SettlementUnclaimCommand extends SettlementSubCommand implements LandOwnerCommand {

    public SettlementUnclaimCommand(SubCommand parent) {
        super(parent, "unclaim [<radius>]");
        registerCommand(new SettlementSubCommand(this, "<radius>", "<#>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                return false;
            }
        });
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        return handleUnclaim(res, res.getSettlement(), base, alias, args);
    }
}
