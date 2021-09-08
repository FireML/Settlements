package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.utils.Utils;

public class VillageDescriptionCommand extends VillageSubCommand {
    public VillageDescriptionCommand(SubCommand parent) {
        super(parent, "setdescription <text>", "description", "setboard", "board");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        res.getVillage().setDescription(Utils.filterAllCurses(String.join(" ", args)));
        DatabaseManager.getInstance().saveSettlement(res.getVillage());
        getMessage("success").tell(res,
                "state", res.getVillage().getState().getTag(),
                "description", res.getVillage().getDescription()
        );
        return true;
    }
}
