package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Chunk;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageSetHomeCommand extends SubCommand {
    public VillageSetHomeCommand() {
        super("Sets the home for the village", "sethome");
        this.setPermission("kingdoms.user");
        this.setUsage("sethome");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"));
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillageRank().below(VillageRank.CHIEF)) {
            MessageUtils.tell(player, Message.get("rank-required"), "rank", VillageRank.CHIEF.getTag(village.getState()));
            return;
        }
        Chunk currentChunk = player.getLocation().getChunk();
        OwnedLand land = LandManager.getInstance().getLand(currentChunk);
        if (land == null || !land.getOwnerVillage().equals(village)) {
            MessageUtils.tell(player, "&c&lYou do not own this land!");
            return;
        }
        MessageUtils.tellResidents(village.getOnlineResidents(),
                Message.get("village-home-set"),
                "rank", res.getVillageRank().getTag(village.getState()),
                "player", player.getName(),
                "x", String.valueOf(player.getLocation().getBlockX()),
                "y", String.valueOf(player.getLocation().getBlockY()),
                "z", String.valueOf(player.getLocation().getBlockZ()));
        village.setVillageHome(player.getLocation());
        Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
    }
}
