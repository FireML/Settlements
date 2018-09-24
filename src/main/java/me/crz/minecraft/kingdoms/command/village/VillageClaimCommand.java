package me.crz.minecraft.kingdoms.command.village;

import org.bukkit.World;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
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

public class VillageClaimCommand extends SubCommand
{
    public VillageClaimCommand() {
        super("Claim a chunk of land", new String[] { "claim" });
        this.setPermission("village.user");
        this.setUsage("claim");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + " or higher&r&c&l!", null, null);
            return;
        }
        if (!Utils.isWorldClaimable(player.getWorld())) {
            MessageUtils.tell(player, "&c&lClaiming is disabled in this world!", null, null);
            return;
        }
        final int amountClaimed = LandManager.getInstance().getClaimedLandSize(village);
        final int maxClaimAllowed = Utils.getChunkMax(village.getLevel(), village.getState());
        if (amountClaimed >= maxClaimAllowed) {
            MessageUtils.tell(player, MessageUtils.CANT_CLAIM_MORE_MESSAGE, "{max}", Integer.toString(maxClaimAllowed));
            return;
        }
        final Chunk currentChunk = player.getLocation().getChunk();
        final OwnedLand land = LandManager.getInstance().getLand(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld().getName());
        if (amountClaimed > 0) {
            int landAround = 0;
            for (final Chunk chunk : this.getChunksAroundPlayer(player)) {
                final OwnedLand checkLand = LandManager.getInstance().getLand(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
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
        for (final Chunk chunk2 : this.getChunksAroundPlayer0(player)) {
            final OwnedLand checkLand2 = LandManager.getInstance().getLand(chunk2.getX(), chunk2.getZ(), chunk2.getWorld().getName());
            if (checkLand2 == null) {
                continue;
            }
            if (!checkLand2.getOwnerVillage().equals(village.getName())) {
                MessageUtils.tell(player, "&c&lERROR &r&7You are too close to &e{owner}'s land !", "{owner}", checkLand2.getOwnerVillage());
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
    
    public Collection<Chunk> getChunksAroundPlayer(final Player player) {
        final int[] offset = { -1, 0, 1 };
        final World world = player.getWorld();
        final int baseX = player.getLocation().getChunk().getX();
        final int baseZ = player.getLocation().getChunk().getZ();
        final Collection<Chunk> chunksAroundPlayer = new HashSet<Chunk>();
        int[] array;
        for (int length = (array = offset).length, i = 0; i < length; ++i) {
            final int x = array[i];
            int[] array2;
            for (int length2 = (array2 = offset).length, j = 0; j < length2; ++j) {
                final int z = array2[j];
                final Chunk chunk = world.getChunkAt(baseX + x, baseZ + z);
                chunksAroundPlayer.add(chunk);
            }
        }
        return chunksAroundPlayer;
    }
    
    public Collection<Chunk> getChunksAroundPlayer0(final Player player) {
        final int[] offset = { -4, -5, -1, 0, 1, 2, 3, 4 };
        final World world = player.getWorld();
        final int baseX = player.getLocation().getChunk().getX();
        final int baseZ = player.getLocation().getChunk().getZ();
        final Collection<Chunk> chunksAroundPlayer = new HashSet<Chunk>();
        int[] array;
        for (int length = (array = offset).length, i = 0; i < length; ++i) {
            final int x = array[i];
            int[] array2;
            for (int length2 = (array2 = offset).length, j = 0; j < length2; ++j) {
                final int z = array2[j];
                final Chunk chunk = world.getChunkAt(baseX + x, baseZ + z);
                chunksAroundPlayer.add(chunk);
            }
        }
        return chunksAroundPlayer;
    }
}
