package de.themoep.settlements.command.plot;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.ChatColor;

public class PlotNameCommand extends PlotSubCommand {
    public PlotNameCommand(PlotCommand parent) {
        super(parent, "name <text>", "setname");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        String name = MessageUtils.replaceColorCodes(Utils.filterAllCurses(String.join(" ", args)));
        if (ChatColor.stripColor(name).length() > 16) {
            getError("too-long").tell(res, "input", name, "amount", "16");
            return true;
        }
        land.setName(name);
        LandManager.getInstance().syncAndSave(land);
        getMessage("success").tell(res, "description", land.getName(), "name", land.getName());
        return true;
    }
}
