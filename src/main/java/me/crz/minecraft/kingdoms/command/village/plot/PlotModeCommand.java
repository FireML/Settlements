package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.PlotMode;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlotModeCommand extends VillageSubCommand {
    public PlotModeCommand(VillagePlotCommand parent) {
        super(parent, "mode <mode> <on/off>", VillageRank.ELDER);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null || !res.getVillage().equals(land.getOwnerVillage())) {
            res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
        } else if (args.length < 2) {
            return false;
        } else {
            try {
                PlotMode m = PlotMode.valueOf(args[0].toLowerCase());
                if (m.getOptions().containsKey(args[1].toLowerCase())) {
                    m.set(m.getOptions().get(args[1].toLowerCase()), land);
                    res.tell(getMessage("success"), "mode", m.name(), "value", args[1]);
                } else {
                    return false;
                }
            } catch (IllegalArgumentException e) {
                res.tell(getMessage("modes"), "modes", Arrays.stream(PlotMode.values()).map(Enum::name).collect(Collectors.joining(", ")));
                return false;
            }
        }
        return true;
    }
}
