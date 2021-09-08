package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.gui.ModeGUI;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.utils.Utils;

import java.util.stream.Collectors;

public class VillageModeCommand extends VillageSubCommand {
    public VillageModeCommand(SubCommand parent) {
        super(parent, "mode <mode> on|off");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            new ModeGUI().open(res.getPlayer());
            return true;
        }

        try {
            PlotMode m = PlotMode.get(args[0], PlotMode.AppliesTo.LAND_OWNER);
            try {
                boolean value = !res.getVillage().getMode(m);
                if (args.length > 1) {
                    value = Utils.parseBoolean(args[1]);
                }
                if (res.getVillage().setMode(m, value)) {
                    DatabaseManager.getInstance().saveSettlement(res.getVillage());
                }
                res.tell(getMessage("success"), "mode", m.name(), "value", String.valueOf(value));
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            res.tell(getMessage("modes"), "modes", PlotMode.values(PlotMode.AppliesTo.LAND_OWNER).stream().map(Enum::name).collect(Collectors.joining(", ")));
            return false;
        }
        return true;
    }
}
