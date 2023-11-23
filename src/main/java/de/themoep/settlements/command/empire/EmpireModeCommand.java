package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.gui.ModeGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;

import java.util.stream.Collectors;

public class EmpireModeCommand extends EmpireSubCommand {
    public EmpireModeCommand(SubCommand parent) {
        super(parent, "mode <mode> on|off");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            new ModeGUI(res, res.getSettlement().getEmpire()).open();
            return true;
        }

        try {
            PlotMode m = PlotMode.get(args[0], PlotMode.AppliesTo.LAND_OWNER);
            try {
                boolean value = !res.getSettlement().getEmpire().getMode(m);
                if (args.length > 1) {
                    value = Utils.parseBoolean(args[1]);
                }
                if (res.getSettlement().getEmpire().setMode(m, value)) {
                    DatabaseManager.getInstance().saveEmpire(res.getSettlement().getEmpire());
                }
                res.tell(getMessage("success").getComponent(), "mode", m.name(), "value", String.valueOf(value));
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (IllegalArgumentException e) {
            res.tell(getMessage("modes").getComponent(), "modes", PlotMode.values(PlotMode.AppliesTo.LAND_OWNER).stream().map(Enum::name).collect(Collectors.joining(", ")));
            return false;
        }
        return true;
    }
}
