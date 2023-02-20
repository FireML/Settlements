package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;

public class SettlementDescriptionCommand extends SettlementSubCommand {
    public SettlementDescriptionCommand(SubCommand parent) {
        super(parent, "setdescription <text>", "description", "setboard", "board");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        res.getSettlement().setDescription(Utils.filterAllCurses(String.join(" ", args)));
        DatabaseManager.getInstance().saveSettlement(res.getSettlement());
        getMessage("success").tell(res,
                "state", res.getSettlement().getState().getTag(),
                "description", res.getSettlement().getDescription()
        );
        return true;
    }
}
