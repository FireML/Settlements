package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotDescriptionCommand extends PlotSubCommand {
    public PlotDescriptionCommand(PlotCommand parent) {
        super(parent, "desc <text>", "description", "board", "setdescription");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        String description = ChatManager.getInstance().filterAllCurses(String.join(" ", args));
        if (description.length() > 255) {
            getError("too-long").tell(res, "input", description, "amount", "255");
            return true;
        }
        land.setDescription(description);
        LandManager.getInstance().syncAndSave(land);
        getMessage("success").tell(res, "description", land.getDescription());
        return true;
    }
}
