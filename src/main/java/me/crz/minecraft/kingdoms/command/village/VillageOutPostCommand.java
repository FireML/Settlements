package me.crz.minecraft.kingdoms.command.village;

import org.bukkit.World;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.ExecuteMoveUtil;
import org.bukkit.Bukkit;
import org.bson.Document;
import java.util.Map;
import org.bukkit.Location;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import org.bukkit.Chunk;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageOutPostCommand extends SubCommand
{
    StringBuilder sb;
    
    public VillageOutPostCommand() {
        super("Claim a seperate chunk of land", new String[] { "outpost" });
        sb = new StringBuilder();
        this.setPermission("village.user");
        sb.append("&8&l--------&r&c&lOutpost Commands&r&8&l--------\n");
        sb.append("&8[&9*&8] &b/s outpost claim &7- claims an outpost for your settlement\n");
        sb.append("&8[&9*&8] &b/s outpost unclaim &7- unclaims the outpost your standing on\n");
        sb.append("&8[&9*&8] &b/s outpost sethome <name> &7- sets a sethome on that specific chunk\n");
        sb.append("&8[&9*&8] &b/s outpost tp <name> &7- teleports to a outpost shown in list\n");
        sb.append("&8[&9*&8] &b/s outpost list &7- lists all of your outposts");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, sb.toString());
            return;
        }
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        if (args[0].equalsIgnoreCase("claim")) {
            if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
                MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + " or higher&r&c&l!", null, null);
                return;
            }
            if (!Utils.isWorldClaimable(player.getWorld())) {
                MessageUtils.tell(player, "&c&lClaiming is disabled in this world!", null, null);
                return;
            }
            final int amountClaimed = LandManager.getInstance().getClaimedLandSizeOutpost(village);
            final int maxClaimAllowed = Utils.getChunkMaxOutpost(village.getLevel(), village.getState());
            if (amountClaimed >= maxClaimAllowed) {
                MessageUtils.tell(player, "&c&lERROR &r&7Your settlement can only claim &e{max}&r&7 outpost chunks!", "{max}", Integer.toString(maxClaimAllowed));
                return;
            }
            final OwnedLand land = LandManager.getInstance().getLandFromLocation(player.getLocation());
            if (land != null) {
                if (land.getOwnerVillage().equals(village.getName())) {
                    MessageUtils.tell(player, MessageUtils.ALREADY_OWN_LAND_MESSAGE, null, null);
                    return;
                }
                MessageUtils.tell(player, MessageUtils.CANT_CLAIM_HERE_MESSAGE, "{owner}", land.getOwnerVillage());
                return;
            }
            else {
                LandManager.getInstance().claimOutpost(player.getLocation(), village);
                MessageUtils.tellResidents(village.getResidents(), "&a&lSUCCESS &r&e{player} &7has claimed an &coutpost&7 for your settlement!", "{player}", player.getName());
            }
        }
        if (args[0].equalsIgnoreCase("unclaim")) {
            if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
                MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + "&r&c&l!");
                return;
            }
            final Chunk currentChunk = player.getLocation().getChunk();
            final OwnedLand land2 = LandManager.getInstance().getLand(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld().getName());
            if (land2 == null || !land2.getOwnerVillage().equals(village.getName())) {
                MessageUtils.tell(player, "&cYou do not own this land.");
                return;
            }
            if (!land2.getIsOutpost()) {
                MessageUtils.tell(player, "&cThis land is not an outpost, please use &7/s unclaim&c to unclaim this land.");
                return;
            }
            boolean landCheck = false;
            for (final Chunk chunk : this.getChunksAroundPlayer(player)) {
                final OwnedLand checkLand = LandManager.getInstance().getLand(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
                if (checkLand == null) {
                    continue;
                }
                if (!checkLand.getOwnerVillage().equals(village.getName()) || checkLand.getIsOutpost()) {
                    continue;
                }
                landCheck = true;
            }
            if (landCheck) {
                MessageUtils.tell(player, "&c&lPlease unclaim all the land surrounding this outpost before you can unclaim this!");
                return;
            }
            LandManager.getInstance().removeLand(land2);
            MessageUtils.tellResidents(village.getResidents(), "&aSuccess unclaimed outpost.");
        }
        if (args[0].equalsIgnoreCase("list")) {
            final StringBuilder sb = new StringBuilder("&7------- &cOutPosts&7 -------\n");
            for (final OwnedLand land2 : LandManager.getInstance().getClaimedLand(village)) {
                if (land2.getIsOutpost()) {
                    if (village.getOutpostHomes().containsKey(String.valueOf(land2.getX()) + "-" + land2.getWorldName() + "-" + land2.getZ())) {
                        final Location loc = (Location)village.getOutpostHomes().get(String.valueOf(land2.getX()) + "-" + land2.getWorldName() + "-" + land2.getZ());
                        String name = "";
                        for (final Map.Entry<String, Document> doc : village.getOutpostHomesList().entrySet()) {
                            if (((Document)doc.getValue()).containsKey((Object)"name") && ((String)doc.getKey()).equals(String.valueOf(land2.getX()) + "-" + land2.getWorldName() + "-" + land2.getZ())) {
                                name = ((Document)doc.getValue()).getString((Object)"name");
                            }
                        }
                        sb.append("&a" + name);
                        sb.append(" (World: ");
                        sb.append(loc.getWorld().getName());
                        sb.append(", X: ");
                        sb.append(loc.getX());
                        sb.append(", Z: ");
                        sb.append(loc.getZ());
                        sb.append(" )\n");
                    }
                    else {
                        sb.append("&aMBFU\n");
                    }
                }
            }
            MessageUtils.tell(player, sb.toString());
        }
        if (args[0].equalsIgnoreCase("tp")) {
            if (args.length < 2) {
                MessageUtils.tell(player, "/s outpost tp <name>");
                return;
            }
            Document doc2 = null;
            for (final Map.Entry<String, Document> document : village.getOutpostHomesList().entrySet()) {
                if (((Document)document.getValue()).containsKey((Object)"name") && ((Document)document.getValue()).getString((Object)"name").equalsIgnoreCase(args[1])) {
                    doc2 = document.getValue();
                    break;
                }
            }
            if (doc2 != null) {
                final Location loc2 = new Location(Bukkit.getWorld(doc2.getString((Object)"worldName")), (double)doc2.getDouble((Object)"x"), (double)doc2.getDouble((Object)"y"), (double)doc2.getDouble((Object)"z"));
                MessageUtils.tell(player, "&e&lTeleporting in 5 seconds...");
                ExecuteMoveUtil.getInstance().execute(player, () -> player.teleport(loc2), 5, "Teleport");
                return;
            }
            MessageUtils.tell(player, "&cunable to find: &6" + args[1] + "\n&cPlease make sure your using the outpost name shown in /s outpost list!\n&ce.g. /s outpost tp outpost-4");
        }
        if (args[0].equalsIgnoreCase("sethome")) {
            if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
                MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + " or higher&r&c&l!", null, null);
                return;
            }
            if (args.length < 2) {
                MessageUtils.tell(player, "/s outpost sethome <name>");
                return;
            }
            final OwnedLand land3 = LandManager.getInstance().getLandFromLocation(player.getLocation());
            if (land3 == null || !land3.getIsOutpost()) {
                MessageUtils.tell(player, "&a&lLand must be an outpost to set a home here!");
                return;
            }
            village.addOutpostHome(player.getLocation(), land3, args[1]);
            VillageManager.getInstance().saveVillage(village);
            MessageUtils.tell(player, "&a&lHome &6" + args[1] + " &aset here");
        }
    }
    
    public Collection<Chunk> getChunksAroundPlayer(final Player player) {
        final int[] offset = { -4, -5, -1, 1, 2, 3, 4 };
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
    
    private static /* synthetic */ void lambda$0(final Player player, final Location location) {
        player.teleport(location);
    }
}
