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
        super(parent, "mode <mode> <value>", VillageRank.ELDER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            ModeGUI.getInstance().open(res.getPlayer());
            return true;
        }

        try {
            PlotMode m = PlotMode.valueOf(args[0].toLowerCase(), PlotMode.AppliesTo.LAND_OWNER);
            if (m.getOptions().containsKey(args[1].toLowerCase())) {
                if (m.set(m.getOptions().get(args[1].toLowerCase()), res.getVillage())) {
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
                }
                res.tell(getMessage("success"), "mode", m.name(), "value", args[1]);
            } else {
                return false;
            }
        } catch (IllegalArgumentException e) {
            res.tell(getMessage("modes"), "modes", PlotMode.values(PlotMode.AppliesTo.LAND_OWNER).stream().map(Enum::name).collect(Collectors.joining(", ")));
            return false;
        }
        return true;
    }
}
