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
        } else if (!land.hasLink()) {
            getError("none-linked").tell(res);
        } else {
            Collection<OwnedLand> toSave = new ArrayList<>();
            int linked = land.getLinked().size() - 1;
            getMessage("disconnected").tell(res, "amount", String.valueOf(linked));
            if (land.removeLink()) {
                Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(land));
            }
        }
        return true;
    }
}
