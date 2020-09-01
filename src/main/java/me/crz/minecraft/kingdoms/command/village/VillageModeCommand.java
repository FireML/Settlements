package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.gui.ModeGUI;

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
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
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
