package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.gui.ModeGUI;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.stream.Collectors;

public class EmpireModeCommand extends EmpireSubCommand {
    public EmpireModeCommand(SubCommand parent) {
        super(parent, "mode <mode> on|off", EmpireRank.SENATOR);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            ModeGUI.getInstance().open(res.getPlayer());
            return true;
        } else if (args.length < 2) {
            return false;
        }

        try {
            PlotMode m = PlotMode.valueOf(args[0].toLowerCase(), PlotMode.AppliesTo.LAND_OWNER);
            try {
                if (res.getVillage().getEmpire().setMode(m, Utils.parseBoolean(args[1]))) {
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(res.getVillage().getEmpire()));
                }
                res.tell(getMessage("success"), "mode", m.name(), "value", args[1]);
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
