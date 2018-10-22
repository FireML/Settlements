package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;

import java.util.ArrayList;
import java.util.Collection;

public class PlotUnlinkCommand extends ResidentSubCommand {
    public PlotUnlinkCommand(VillagePlotCommand parent) {
        super(parent, "unlink", "disconnect");
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
        } else if (land.getLinkedTo() == null && land.getLinked().isEmpty()) {
            getError("none-linked").tell(res);
        } else {
            Collection<OwnedLand> toSave = new ArrayList<>();
            for (SimpleChunkLocation l : land.getLinked()) {
                OwnedLand c = LandManager.getInstance().getLand(l);
                if (c.isLinkedTo(land)) {
                    c.setLinkedTo(null);
                    toSave.add(c);
                }
            }
            OwnedLand lt = LandManager.getInstance().getLand(land.getLinkedTo());
            if (lt != null) {
                getMessage("unlink.self").tell(res, "location", land.getLinkedTo().getKey());
                if (lt.removeLinked(land)) {
                    toSave.add(lt);
                }
                land.setLinkedTo(null);
            }
            if (!land.getLinked().isEmpty()) {
                land.getLinked().clear();
                getMessage("unlink.others").tell(res, "amount", String.valueOf(toSave.size()));
            }
            toSave.add(land); // add to save
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
        }
        return true;
    }
}
