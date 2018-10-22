package me.crz.minecraft.kingdoms.command.village.plot;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.command.village.VillagePlotCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;

public class PlotInfoCommand extends VillageSubCommand {
    public PlotInfoCommand(VillagePlotCommand parent) {
        super(parent, "info", VillageRank.RESIDENT, "here");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        OwnedLand land = LandManager.getInstance().getLand(res.getPlayer().getLocation());
        if (land == null) {
            res.tell(getError("not-a-plot"));
            return true;
        }
        res.tell(getMessage("general"),
                "state", land.getOwnerVillage().getState().getTag(),
                "name", land.getOwnerVillage().getName(),
                "spawning", String.valueOf(land.isMobSpawning()),
                "firespread", String.valueOf(land.isFireSpread()),
                "explosions", String.valueOf(land.isExplosions()),
                "pvp", String.valueOf(land.isPvp()),
                "build", land.getBuildState().getTag()
        );
        if (land.isPlot()) {
            Utils.runAsync(() -> res.tell(getMessage("owner"), "name", ResidentManager.getInstance().getResident(land.getPlotOwner()).getName()));
        } else if (land.isForSale()) {
            res.tell(getMessage("price"), "price", String.valueOf(land.getPrice()));
        }
        return true;
    }
}
