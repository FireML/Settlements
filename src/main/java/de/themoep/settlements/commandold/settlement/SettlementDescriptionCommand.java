package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;

public class SettlementDescriptionCommand extends SettlementSubCommand {
    public SettlementDescriptionCommand(SubCommand parent) {
        super(parent, "setdescription <text>", "description", "setboard", "board");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        res.getSettlement().setDescription(String.join(" ", args));
        DatabaseManager.getInstance().saveSettlement(res.getSettlement());
        getMessage("success").tell(res,
                "state", res.getSettlement().getState().getTag(),
                "description", res.getSettlement().getDescription()
        );
        return true;
    }
}
