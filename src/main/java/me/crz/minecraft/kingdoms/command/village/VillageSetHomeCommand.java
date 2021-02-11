package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import org.bukkit.entity.Player;

public class VillageSetHomeCommand extends VillageSubCommand {
    public VillageSetHomeCommand(SubCommand parent) {
        super(parent, "sethome");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Village village = res.getVillage();
        Player player = res.getPlayer();
        OwnedLand land = LandManager.getInstance().getLand(player.getLocation());
        if (land == null || !land.getLandOwner().equals(village)) {
            getError("not-your-village").tell(res, "state", res.getVillage().getState().getTag());
            return true;
        }

        if (village.getHome() != null && !land.containsHome()) {
            OwnedLand home = LandManager.getInstance().getLand(village.getHome().toChunkLocation());
            if (home != null) {
                if (land.isOutpost()) {
                    NamedLocation outpost = village.getOutpostHomes().get(land.getOutpostName().toLowerCase());
                    if (outpost != null) {
                        land.setOutpost(null);
                        outpost.copy(village.getHome());
                        home.setOutpost(outpost.getName());
                        getMessage("moved-outpost").tell(village.getOnlineResidents(),
                                "rank", res.getVillageRank().getTag(village),
                                "title", res.getVillageTitle(),
                                "displaytitle", res.getDisplayTitle(),
                                "name", player.getName(),
                                "outpost", outpost.getName(),
                                "state", village.getState().getTag(),
                                "world", outpost.getWorld(),
                                "x", String.valueOf(outpost.getBlockX()),
                                "y", String.valueOf(outpost.getBlockY()),
                                "z", String.valueOf(outpost.getBlockZ()));
                    }
                }

                if (!home.isOutpost() && LandManager.getInstance().getAllConnectedLand(home,
                        l -> l.equals(land) || l.isOutpost() ? LandManager.FilterResponse.RETURN : LandManager.FilterResponse.CONTINUE)
                        .isEmpty()) {
                    getError("abandoned-land").tell(res, "state", res.getVillage().getState().getTag());
                    return true;
                }
            }
        }

        village.setHome(player.getLocation());
        getMessage("success").tell(village.getOnlineResidents(),
                "rank", res.getVillageRank().getTag(village),
                "title", res.getVillageTitle(),
                "name", player.getName(),
                "state", village.getState().getTag(),
                "world", player.getLocation().getWorld().getName(),
                "x", String.valueOf(player.getLocation().getBlockX()),
                "y", String.valueOf(player.getLocation().getBlockY()),
                "z", String.valueOf(player.getLocation().getBlockZ()));
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        return true;
    }
}
