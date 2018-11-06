package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotUnlinkCommand extends PlotSubCommand {
    public PlotUnlinkCommand(PlotCommand parent) {
        super(parent, "unlink", "disconnect");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        if (!land.hasLink()) {
            getError("none-linked").tell(res);
        } else {
            int linked = land.getLinked().size() - 1;
            getMessage("disconnected").tell(res, "amount", String.valueOf(linked));
            if (land.removeLink()) {
                LandManager.getInstance().saveLand(land);
            }
        }
        return true;
    }
}
