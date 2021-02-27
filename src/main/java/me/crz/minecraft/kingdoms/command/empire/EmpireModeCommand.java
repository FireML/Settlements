package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.gui.ModeGUI;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;

import java.util.stream.Collectors;

public class EmpireModeCommand extends EmpireSubCommand {
    public EmpireModeCommand(SubCommand parent) {
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
                boolean value = !res.getVillage().getEmpire().getMode(m);
                if (args.length > 1) {
                    value = Utils.parseBoolean(args[1]);
                }
                if (res.getVillage().getEmpire().setMode(m, value)) {
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(res.getVillage().getEmpire()));
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
