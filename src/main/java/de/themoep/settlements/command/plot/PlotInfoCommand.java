package de.themoep.settlements.command.plot;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.utils.Utils;

import java.util.Collection;
import java.util.stream.Collectors;

public class PlotInfoCommand extends ResidentSubCommand {
    public PlotInfoCommand(PlotCommand parent) {
        super(parent, "info", "here", "show");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            res.tell(getError("not-a-claim"));
            return true;
        }
        res.tell(getMessage("general"), land.getReplacements());
        if (!land.getLandOwner().isAbandoned()) {
            if (land.isPlot()) {
                Utils.runAsync(() -> res.tell(getMessage("owner"), "name", ResidentManager.getInstance().getResident(land.getPlotOwner()).getName()));
            } else if (land.isForSale() &&
                    (land.isEmbassy() || land.isOwnedBy(res))) {
                res.tell(getMessage("price"), "price", String.valueOf(land.getPrice()));
            }
            if (land.isPlot() || land.isForSale()) {
                res.tell(getMessage("tax"), "tax", String.valueOf(land.getTax()));
            }
        }
        if (land.hasLink()) {
            Collection<OwnedLand> linked = land.getLinked();
            getMessage("link").tell(res,
                    "id", land.getLink().toString(),
                    "amount", String.valueOf(linked.size() - 1),
                    "linked", linked.stream().filter(o -> !o.equals(land)).map(o -> o.getX() + "/" + o.getZ()).collect(Collectors.joining("\n")),
                    "x", String.valueOf(land.getX()),
                    "z", String.valueOf(land.getZ())
            );
        }
        return true;
    }
}
