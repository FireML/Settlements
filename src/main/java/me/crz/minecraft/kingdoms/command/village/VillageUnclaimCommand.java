package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageUnclaimCommand extends SubCommand {
    public VillageUnclaimCommand() {
        super("Unclaim land which is claimed by your village", "unclaim");
        this.setPermission("kingdoms.user");
        this.setUsage("unclaim");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"));
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillageRank().below(VillageRank.ELDER)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + "&r&c&l!");
            return;
        }
        OwnedLand land = LandManager.getInstance().getLand(player.getLocation());
        if (land == null || !land.getOwnerVillage().equals(village)) {
            MessageUtils.tell(player, "&cYou do not own this land.");
            return;
        }
        if (land.getIsOutpost()) {
            MessageUtils.tell(player, "&cThis land is an outpost, please use &7/s outpost unclaim&c to unclaim this outpost.");
            return;
        }
        LandManager.getInstance().unclaimLand(land);
        MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("success-unclaim-land"),
                "player", player.getName(),
                "x", String.valueOf(player.getLocation().getBlockX()),
                "z", String.valueOf(player.getLocation().getBlockZ()));
    }
}
