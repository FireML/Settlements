package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.UtilChat;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotNameCommand extends PlotSubCommand {
    public PlotNameCommand(PlotCommand parent) {
        super(parent, "name <text>", "board", "description", "setdescription");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        land.setDescription(UtilChat.getInstance().filterCurses(String.join(" ", args)));
        LandManager.getInstance().saveLand(land);
        getMessage("success").tell(res, "description", land.getDescription());
        return true;
    }
}
