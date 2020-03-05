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
        String name = ChatManager.getInstance().filterAllCurses(String.join(" ", args));
        if (name.length() > 16) {
            getError("too-long").tell(res, "input", name, "amount", "16");
            return true;
        }
        land.setName(name);
        LandManager.getInstance().syncAndSave(land);
        getMessage("success").tell(res, "description", land.getName(), "name", land.getName());
        return true;
    }
}
