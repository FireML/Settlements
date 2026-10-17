package de.themoep.settlements.command.plot;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.StringUtils;
import de.themoep.settlements.utils.Utils;

public class PlotDescriptionCommand extends PlotSubCommand {
    public PlotDescriptionCommand(PlotCommand parent) {
        super(parent, "desc <text>", "description", "board", "setdescription");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        String description = StringUtils.parseColors(String.join(" ", args));
        if (StringUtils.stripColors(description).length() > 255) {
            getError("too-long").tell(res, "input", description, "amount", "255");
            return true;
        }
        land.setDescription(description);
        LandManager.getInstance().syncAndSave(land);
        getMessage("success").tell(res, "description", land.getDescription());
        return true;
    }
}
