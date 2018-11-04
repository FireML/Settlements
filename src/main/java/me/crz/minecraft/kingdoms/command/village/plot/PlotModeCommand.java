package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.gui.ModeGUI;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlotModeCommand extends ResidentSubCommand {
    public PlotModeCommand(VillagePlotCommand parent) {
        super(parent, "mode <mode> <on/off>", "set");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            getError("not-a-plot").tell(res);
        } else if (!(land.isEmbassy() || land.isOwnedBy(res.getVillage()))) {
            getError("not-your-village").tell(res, "state", land.getOwnerVillage().getState().getTag());
        } else if (args.length < 1) {
            ModeGUI.getInstance().open(res.getPlayer());
        } else if (args.length > 2) {
            return false;
        } else {
            boolean canAdmin = res.hasVillage() && land.isOwnedBy(res.getVillage()) && res.getVillageRank().atLeast(VillageRank.ELDER);
            if (!land.isPlotOwner(res) && !canAdmin) {
                getError("not-your-plot").tell(res, "state", res.getVillage().getState().getTag());
                return true;
            }
            try {
                PlotMode m = PlotMode.valueOf(args[0].toLowerCase());
                if (m == PlotMode.embassy) {
                    if (land.containsHome() || land.isOutpost()) {
                        getError("cant-set-embassy").tell(res);
                        return true;
                    } else if (!canAdmin) {
                        return true;
                    }
                }
                Boolean setTo = !m.get(land);
                if (args.length > 1) {
                    setTo = m.getOptions().get(args[1].toLowerCase());
                    if (setTo == null) {
                        if (m == PlotMode.embassy) {
                            try {
                                int price = Integer.parseInt(args[1]);
                                setTo = true;
                                land.setForSale(true);
                                land.setPrice(price);
                            } catch (NumberFormatException e) {
                                getError("not-a-number").tell(res, "input", args[1]);
                                return true;
                            }
                        } else {
                            return false;
                        }
                    }
                }
                m.set(setTo, land);
                LandManager.getInstance().syncAndSave(land);
                res.tell(getMessage("success"), "mode", m.name(), "value", args.length > 1 ? args[1] : String.valueOf(setTo));
            } catch (IllegalArgumentException e) {
                res.tell(getMessage("modes"), "modes", Arrays.stream(PlotMode.values()).map(Enum::name).collect(Collectors.joining(", ")));
                return false;
            }
        }
        return true;
    }
}
