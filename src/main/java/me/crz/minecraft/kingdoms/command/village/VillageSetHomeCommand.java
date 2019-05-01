package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
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
            res.tell(getError("not-your-village"), "state", res.getVillage().getState().getTag());
            return true;
        }

        if (!land.containsHome() && LandManager.getInstance().getAllConnectedLand(land, l -> {
            if (l.containsHome()) {
                return LandManager.FilterResponse.RETURN;
            }
            return LandManager.FilterResponse.CONTINUE;
        }).isEmpty()) {
            res.tell(getError("not-connected-to-home"), "state", res.getVillage().getState().getTag());
            return true;
        }

        village.setHome(player.getLocation());
        MessageUtils.tellResidents(village.getOnlineResidents(),
                getMessage("success"),
                "rank", res.getVillageRank().getTag(village),
                "title", res.getTitle(),
                "name", player.getName(),
                "state", res.getVillage().getState().getTag(),
                "world", player.getLocation().getWorld().getName(),
                "x", String.valueOf(player.getLocation().getBlockX()),
                "y", String.valueOf(player.getLocation().getBlockY()),
                "z", String.valueOf(player.getLocation().getBlockZ()));
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
        return true;
    }
}
