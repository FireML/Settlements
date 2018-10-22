package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;

import java.util.ArrayList;
import java.util.Collection;

public class PlotUnlinkCommand extends VillageSubCommand {
    public PlotUnlinkCommand(VillagePlotCommand parent) {
        super(parent, "unlink", VillageRank.RESIDENT, "disconnect");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null || !res.getVillage().equals(land.getOwnerVillage())) {
            getError("not-your-village").tell(res, "state", res.getVillage().getState().getTag());
        } else if (!land.isPlotOwner(res.getUUID())) {
            getError("not-your-plot").tell(res);
        } else if (land.getLinkedTo() == null && land.getLinked().isEmpty()) {
            getError("none-linked").tell(res);
        } else {
            Collection<OwnedLand> connected = new ArrayList<>();
            for (SimpleChunkLocation l : land.getLinked()) {
                OwnedLand c = LandManager.getInstance().getLand(l);
                if (c.isLinkedTo(land)) {
                    c.setLinkedTo(null);
                    connected.add(c);
                }
            }
            if (land.getLinked() != null) {
                getMessage("unlink.self").tell(res, "location", land.getLinkedTo().getKey());
                land.setLinkedTo(null);
            }
            if (!land.getLinked().isEmpty()) {
                land.getLinked().clear();
                getMessage("unlink.others").tell(res, "amount", String.valueOf(connected.size()));
            }
            connected.add(land); // add to save
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(connected));
        }
        return true;
    }
}
