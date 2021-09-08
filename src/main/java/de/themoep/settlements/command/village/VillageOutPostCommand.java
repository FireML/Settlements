package de.themoep.settlements.command.village;

import de.themoep.settlements.command.village.outpost.OutpostClaimCommand;
import de.themoep.settlements.command.village.outpost.OutpostListCommand;
import de.themoep.settlements.command.village.outpost.OutpostRenameCommand;
import de.themoep.settlements.command.village.outpost.OutpostSethomeCommand;
import de.themoep.settlements.command.village.outpost.OutpostTeleportCommand;
import de.themoep.settlements.command.village.outpost.OutpostUnclaimCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.HelpCommand;
import de.themoep.settlements.command.SubCommand;

public class VillageOutPostCommand extends VillageSubCommand {

    public VillageOutPostCommand(SubCommand parent) {
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
