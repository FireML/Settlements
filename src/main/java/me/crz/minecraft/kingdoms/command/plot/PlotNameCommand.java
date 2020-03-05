package me.crz.minecraft.kingdoms.command.plot;

import me.crz.minecraft.kingdoms.chat.ChatManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotNameCommand extends PlotSubCommand {
    public PlotNameCommand(PlotCommand parent) {
        super(parent, "name <text>", "setname");
    }

    @Override
    public boolean run(Resident res, OwnedLand land, String base, String alias, String... args) {
        land.setName(ChatManager.getInstance().filterAllCurses(String.join(" ", args)));
        LandManager.getInstance().syncAndSave(land);
        getMessage("success").tell(res, "description", land.getName(), "name", land.getName());
        return true;
    }
}
