package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.HelpCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.settlement.outpost.*;
import de.themoep.settlements.resident.Resident;

public class SettlementOutPostCommand extends SettlementSubCommand {

    public SettlementOutPostCommand(SubCommand parent) {
        super(parent, "outpost <options>");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new OutpostClaimCommand(this));
        registerCommand(new OutpostUnclaimCommand(this));
        registerCommand(new OutpostRenameCommand(this));
        registerCommand(new OutpostSethomeCommand(this));
        registerCommand(new OutpostTeleportCommand(this));
        registerCommand(new OutpostListCommand(this));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return false;
    }
}
