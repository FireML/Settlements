package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Collection;

public class PlotLinkCommand extends ResidentSubCommand {
    public PlotLinkCommand(VillagePlotCommand parent) {
        super(parent, "link", "connect");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
        } else if (!(land.isEmbassy() || res.getVillage().equals(land.getOwnerVillage()))) {
            res.tell(getError("not-your-village"), "state", land.getOwnerVillage().getState().getTag());
        } else if (!land.isPlotOwner(res.getUUID())) {
            getError("not-your-plot").tell(res);
        } else if (land.getLinkedTo() != null) {
            getError("already-linked").tell(res, "location", land.getLinkedTo().getSimpleKey());
        } else {
            Collection<OwnedLand> connected = LandManager.getInstance().getAllConnectedLand(land, location -> {
                if (!land.isPlotOwner(res) || land.getLinkedTo() != null) {
                    return LandManager.FilterResponse.SKIP_ENTRY;
                }
                return LandManager.FilterResponse.ACCEPT;
            });
            for (OwnedLand c : connected) {
                c.setLinkedTo(land);
                land.addLinked(c);
            }
            connected.add(land); // add to save
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(connected));
            getMessage("success").tell(res, "amount", String.valueOf(connected.size()));
        }
        return true;
    }
}
