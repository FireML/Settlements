package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Message;
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
        this.setPermission("kingdoms.user");
        this.setUsage("claim");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"), null, null);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillageRank().below(VillageRank.ELDER)) {
            MessageUtils.tell(player, Message.get("rank-required"), "rank", VillageRank.ELDER.getTag(village.getState()));
            return;
        }
        if (!Utils.isWorldClaimable(player.getWorld())) {
            MessageUtils.tell(player, "&c&lClaiming is disabled in this world!", null, null);
            return;
        }
        int amountClaimed = LandManager.getInstance().getClaimedLandSize(village);
        int maxClaimAllowed = Utils.getChunkMax(village.getLevel(), village.getState());
        if (amountClaimed >= maxClaimAllowed) {
            MessageUtils.tell(player, Message.get("cant-claim-more"), "{max}", Integer.toString(maxClaimAllowed));
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
                if (!checkLand.getOwnerVillage().equals(village)) {
                    continue;
                }
                ++landAround;
            }
            if (landAround == 0) {
                MessageUtils.tell(player, Message.get("no-land-around"), null, null);
                return;
            }
        }
        for (Chunk chunk : Utils.getChunksAroundPlayer(player, 4)) {
            OwnedLand checkLand = LandManager.getInstance().getLand(chunk.getX(), chunk.getZ(), chunk.getWorld());
            if (checkLand == null) {
                continue;
            }
            if (!checkLand.getOwnerVillage().equals(village)) {
                MessageUtils.tell(player, "&c&lERROR &r&7You are too close to &e{owner}'s land !", "{owner}", checkLand.getOwnerVillage().getName());
                return;
            }
        }
        if (land == null) {
            OwnedLand newLand = LandManager.getInstance().claimLand(player.getLocation(), village);
            MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("success-claim-land"),
                    "{player}", player.getName(), "{x}", String.valueOf(newLand.getX()), "{z}",  String.valueOf(newLand.getZ()));
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(newLand));
            return;
        }
        if (land.getOwnerVillage().equals(village)) {
            MessageUtils.tell(player, Message.get("already-own-land"), null, null);
            return;
        }
        MessageUtils.tell(player, Message.get("cant-claim-here"), "{owner}", land.getOwnerVillage().getName());
    }

}
