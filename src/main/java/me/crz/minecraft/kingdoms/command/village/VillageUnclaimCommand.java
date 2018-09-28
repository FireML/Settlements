package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.land.OwnedLand;
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

public class VillageUnclaimCommand extends SubCommand {
    public VillageUnclaimCommand() {
        super("Unclaim land which is claimed by your village", "unclaim");
        this.setPermission("village.user");
        this.setUsage("unclaim");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + "&r&c&l!");
            return;
        }
        final Chunk currentChunk = player.getLocation().getChunk();
        final OwnedLand land = LandManager.getInstance().getLand(currentChunk);
        if (land == null || !land.getOwnerVillage().equals(village.getName())) {
            MessageUtils.tell(player, "&cYou do not own this land.");
            return;
        }
        if (land.getIsOutpost()) {
            MessageUtils.tell(player, "&cThis land is an outpost, please use &7/s outpost unclaim&c to unclaim this outpost.");
            return;
        }
        LandManager.getInstance().unclaimLand(land);
        MessageUtils.tellResidents(village.getOnlineResidents(), "&aSuccess unclaim land.");
    }
}
