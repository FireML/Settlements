package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.land.PlotMode;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.gui.ModeGUI;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.stream.Collectors;

public class PlotModeCommand extends PlotSubCommand {
    public PlotModeCommand(PlotCommand parent) {
        super(parent, "mode <mode> on|off|parent", "set");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length < 1) {
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
                PlotMode m = PlotMode.get(args[0], PlotMode.AppliesTo.LAND);
                if (m == PlotMode.embassy) {
                    if (land.containsHome() || land.isOutpost()) {
                        getError("cant-set-embassy").tell(res);
                        return true;
                    } else if (!canAdmin) {
                        return true;
                    }
                } else if (args.length == 1) {
                    return false;
                }

                if (m == PlotMode.embassy) {
                    try {
                        if (args.length > 1){
                            int price = Integer.parseInt(args[1]);
                            land.setForSale(true);
                            land.setPrice(price);
                        }
                        land.setEmbassy(true);
                        getMessage("success").tell(res, "mode", m.getTag(), "value", "true");
                    } catch (NumberFormatException e) {
                        getError("not-a-number").tell(res, "input", args[1]);
                        return true;
                    }
                } else {
                    try {
                        m.set(Utils.parseBoolean(args[1]), res.getVillage().getEmpire());
                        res.tell(getMessage("success"), "mode", m.getTag(), "value", args[1]);
                    } catch (IllegalArgumentException e) {
                        if (m.appliesTo(PlotMode.AppliesTo.LAND_OWNER) && ("reset".equalsIgnoreCase(args[1]) || "unset".equalsIgnoreCase(args[1]) || "parent".equalsIgnoreCase(args[1]))) {
                            land.unsetMode(m);
                            getMessage("unset").tell(res,
                                    "mode", m.getTag(),
                                    "value", String.valueOf(land.getLandOwner().getMode(m)),
                                    "parent", land.getLandOwner().getStateTag()
                            );
                        } else {
                            return false;
                        }
                    }
                }
                LandManager.getInstance().syncAndSave(land);
            } catch (IllegalArgumentException e) {
                res.tell(getMessage("modes"), "modes", PlotMode.values(PlotMode.AppliesTo.LAND).stream().map(Enum::name).collect(Collectors.joining(", ")));
                return false;
            }
        }
        return true;
    }
}
