package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.land.gui.SettingsGUI;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

public class PlotSettingsCommand extends VillageSubCommand {
    public PlotSettingsCommand(SubCommand parent) {
        super(parent, "settings", VillageRank.RESIDENT);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            getError("not-your-village").tell(res, "state", res.getVillage().getState().getTag());
        } else if (!res.getVillage().equals(land.getOwnerVillage())) {
            getError("not-your-village").tell(res, "state", land.getOwnerVillage().getState().getTag());
        } else if (args.length < 1) {
            SettingsGUI.getInstance().open(res.getPlayer());
            return true;
        }
        return true;
    }
}
