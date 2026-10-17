package de.themoep.settlements.command.plot;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.land.PlotMode;
import de.themoep.settlements.land.gui.plot.ModeGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.Utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlotModeCommand extends PlotSubCommand {
    private final PlotSubCommand embassyCommand;

    public PlotModeCommand(PlotCommand parent) {
        super(parent, "mode <mode> on|off|parent", "set");
        embassyCommand = new PlotSubCommand(this, "embassy <amount>", true) {
            @Override
            public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
                if (land.containsHome() || land.isOutpost()) {
                    getError("cant-set-embassy").tell(res);
                    return true;
                }
                try {
                    land.setEmbassy(true);
                    if (args.length > 0) {
                        int price = Integer.parseInt(args[0]);
                        land.setForSale(true);
                        land.setPrice(price);
                        getMessage("success").tell(res, "mode", PlotMode.embassy.getTag(), "value", String.valueOf(land.getPrice()));
                    } else {
                        getMessage("success-no-sale").tell(res, "mode", PlotMode.embassy.getTag(), "value", String.valueOf(land.getPrice()));
                    }
                    LandManager.getInstance().saveLand(land);
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(res, "input", args[1]);
                }
                return true;
            }
        };
        registerCommand(embassyCommand);
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (args.length < 1) {
            new ModeGUI(res, land).open();
            return true;
        }

        boolean canAdmin = land.canAdmin(res);
        if (!land.isPlotOwner(res) && !canAdmin) {
            getError("not-your-plot").tell(res, "state", res.getSettlement().getState().getTag());
            return true;
        }
        try {
            PlotMode m = PlotMode.get(args[0], PlotMode.AppliesTo.LAND);
            if (m == PlotMode.embassy) {
                embassyCommand.run(res, land, base + " embassy", args[0], Arrays.copyOfRange(args, 1, args.length));
                return true;
            }
            try {
                boolean value = !land.getMode(m);
                if (args.length > 1) {
                    value = Utils.parseBoolean(args[1]);
                }
                land.setMode(m, value);
                res.tell(getMessage("success").getText(), "mode", m.getTag(), "value", String.valueOf(value));
            } catch (IllegalArgumentException e) {
                if (m.appliesTo(PlotMode.AppliesTo.LAND_OWNER) && ("reset".equalsIgnoreCase(args[1]) || "unset".equalsIgnoreCase(args[1]) || "parent".equalsIgnoreCase(args[1]))) {
                    land.unsetMode(m);
                    getMessage("unset").tell(res,
                            "mode", m.getTag(),
                            "value", String.valueOf(land.getSettlement().getMode(m)),
                            "parent", land.getSettlement().getStateTag()
                    );
                } else {
                    return false;
                }
            }
            LandManager.getInstance().syncAndSave(land);
        } catch (IllegalArgumentException e) {
            res.tell(getMessage("modes").getText(), "modes", PlotMode.values(PlotMode.AppliesTo.LAND).stream().map(Enum::name).collect(Collectors.joining(", ")));
            return false;
        }
        return true;
    }
}
