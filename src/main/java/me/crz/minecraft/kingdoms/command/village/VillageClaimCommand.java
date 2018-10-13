package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

public class VillageClaimCommand extends VillageSubCommand {
    public VillageClaimCommand(SubCommand parent) {
        super(parent, "claim", VillageRank.ELDER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Player player = res.getPlayer();
        if (!Utils.isWorldClaimable(player.getWorld())) {
            res.tell(getError("world-not-claimable"), "world", player.getWorld().getName());
            return true;
        }
        Village village = res.getVillage();
        int amountClaimed = LandManager.getInstance().getClaimedLandSize(village);
        int maxClaimAllowed = village.getMaxClaimableChunks();
        if (amountClaimed >= maxClaimAllowed) {
            res.tell(getError("cant-claim-more"), "max", String.valueOf(maxClaimAllowed));
            return true;
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
                res.tell(getError("no-land-around"));
                return true;
            }
        }
        for (Chunk chunk : Utils.getChunksAroundPlayer(player, 4)) {
            OwnedLand checkLand = LandManager.getInstance().getLand(chunk.getX(), chunk.getZ(), chunk.getWorld());
            if (checkLand == null) {
                continue;
            }
            if (!checkLand.getOwnerVillage().equals(village)) {
                res.tell(getError("claim-too-close"),
                        "owner", checkLand.getOwnerVillage().getName(),
                        "state", checkLand.getOwnerVillage().getState().getTag()
                );
                return true;
            }
        }
        if (land == null) {
            OwnedLand newLand = LandManager.getInstance().claimLand(player.getLocation(), village);
            MessageUtils.tellResidents(village.getOnlineResidents(), getMessage("success"),
                    "player", player.getName(),
                    "world", newLand.getWorldName(),
                    "x", String.valueOf(newLand.getX()),
                    "z",  String.valueOf(newLand.getZ()));
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(newLand));
        } else if (land.getOwnerVillage().equals(village)) {
            MessageUtils.tell(player, getError("already-own-land"));
        } else {
            MessageUtils.tell(player, getError("cant-claim-here"),
                    "owner", land.getOwnerVillage().getName(),
                    "state", land.getOwnerVillage().getState().getTag()
            );
        }
        return true;
    }

}
