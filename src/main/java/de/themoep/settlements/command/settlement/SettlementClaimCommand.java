package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.LandOwnerCommand;
import de.themoep.settlements.resident.Resident;

public class SettlementClaimCommand extends SettlementSubCommand implements LandOwnerCommand {
    public SettlementClaimCommand(SubCommand parent) {
        super(parent, "claim [<radius>]");
        registerCommand(new SettlementSubCommand(this, "<radius>", "<#>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                return false;
            }
        });
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        return handleClaim(res, res.getSettlement(), args);
    }

}
