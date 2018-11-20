package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.gui.ModeGUI;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.stream.Collectors;

public class VillageModeCommand extends VillageSubCommand {
    public VillageModeCommand(SubCommand parent) {
        super(parent, "mode <mode> on|off", VillageRank.ELDER);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            ModeGUI.getInstance().open(res.getPlayer());
            return true;
        }

        try {
            PlotMode m = PlotMode.valueOf(args[0].toLowerCase(), PlotMode.AppliesTo.LAND_OWNER);
            try {
                if (res.getVillage().setMode(m, Utils.parseBoolean(args[1]))) {
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
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
