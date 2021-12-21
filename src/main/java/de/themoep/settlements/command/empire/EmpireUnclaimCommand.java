package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.LandOwnerCommand;
import de.themoep.settlements.resident.Resident;

public class EmpireUnclaimCommand extends EmpireSubCommand implements LandOwnerCommand {

    public EmpireUnclaimCommand(SubCommand parent) {
        super(parent, "unclaim [<radius>]");
        registerCommand(new EmpireSubCommand(this, "<radius>", "<#>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                return false;
            }
        });
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        return handleUnclaim(res, res.getSettlement().getEmpire(), base, alias, args);
    }
}
