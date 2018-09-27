package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Chunk;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageClaimCommand extends SubCommand {
    public VillageClaimCommand() {
        super("Claim a chunk of land", "claim");
        this.setPermission("village.user");
        this.setUsage("claim");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + " or higher&r&c&l!", null, null);
            return;
        }
        if (!Utils.isWorldClaimable(player.getWorld())) {
            MessageUtils.tell(player, "&c&lClaiming is disabled in this world!", null, null);
            return;
        }
        int amountClaimed = LandManager.getInstance().getClaimedLandSize(village);
        int maxClaimAllowed = Utils.getChunkMax(village.getLevel(), village.getState());
        if (amountClaimed >= maxClaimAllowed) {
            MessageUtils.tell(player, MessageUtils.CANT_CLAIM_MORE_MESSAGE, "{max}", Integer.toString(maxClaimAllowed));
            return;
        }
        Chunk currentChunk = player.getLocation().getChunk();
        OwnedLand land = LandManager.getInstance().getLand(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld());
        if (amountClaimed > 0) {
            int landAround = 0;
            for (Chunk chunk : Utils.getChunksAroundPlayer(player, 1)) {
                OwnedLand checkLand = LandManager.getInstance().getLand(chunk.getX(), chunk.getZ(), chunk.getWorld());
                if (checkLand == null) {
                    continue;
                }
                if (!checkLand.getOwnerVillage().equals(village.getName())) {
                    continue;
                }
                ++landAround;
            }
            if (landAround == 0) {
                MessageUtils.tell(player, MessageUtils.NO_LAND_AROUND_MESSAGE, null, null);
                return;
            }
        }
        for (Chunk chunk : Utils.getChunksAroundPlayer(player, 4)) {
            OwnedLand checkLand = LandManager.getInstance().getLand(chunk.getX(), chunk.getZ(), chunk.getWorld());
            if (checkLand == null) {
                continue;
            }
            if (!checkLand.getOwnerVillage().equals(village.getName())) {
                MessageUtils.tell(player, "&c&lERROR &r&7You are too close to &e{owner}'s land !", "{owner}", checkLand.getOwnerVillage());
                return;
            }
        }
        if (land == null) {
            LandManager.getInstance().claimLand(player.getLocation(), village);
            MessageUtils.tellResidents(village.getResidents(), MessageUtils.SUCCESS_CLAIM_LAND_MESSAGE, "{player}", player.getName());
            return;
        }
        if (land.getOwnerVillage().equals(village.getName())) {
            MessageUtils.tell(player, MessageUtils.ALREADY_OWN_LAND_MESSAGE, null, null);
            return;
        }
        MessageUtils.tell(player, MessageUtils.CANT_CLAIM_HERE_MESSAGE, "{owner}", land.getOwnerVillage());
    }

}
