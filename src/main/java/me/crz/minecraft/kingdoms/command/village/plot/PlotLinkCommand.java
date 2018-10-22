package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Collection;

public class PlotLinkCommand extends VillageSubCommand {
    public PlotLinkCommand(VillagePlotCommand parent) {
        super(parent, "link", VillageRank.RESIDENT, "connect");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null || !res.getVillage().equals(land.getOwnerVillage())) {
            getError("not-your-village").tell(res, "state", res.getVillage().getState().getTag());
        } else if (!land.isPlotOwner(res.getUUID())) {
            getError("not-your-plot").tell(res);
        } else if (land.getLinkedTo() != null) {
            getError("already-linked").tell(res, "location", land.getLinkedTo().getSimpleKey());
        } else {
            Collection<OwnedLand> connected = LandManager.getInstance().getAllConnectedLand(land, location -> {
                if (!land.isPlotOwner(res) || land.getLinkedTo() != null) {
                    return LandManager.FilterResponse.SKIP;
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
