package me.crz.minecraft.kingdoms.command.village;

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

public class VillageOutPostCommand extends SubCommand {
    private StringBuilder sb;

    public VillageOutPostCommand() {
        super("Claim a seperate chunk of land", "outpost");
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
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, sb.toString());
            return;
        }
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (args[0].equalsIgnoreCase("claim")) {
            if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.ELDER)) {
                MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.ELDER.getTag(village.getState()) + " or higher&r&c&l!", null, null);
                return;
            }
            if (!Utils.isWorldClaimable(player.getWorld())) {
                MessageUtils.tell(player, "&c&lClaiming is disabled in this world!", null, null);
                return;
            }
            int amountClaimed = LandManager.getInstance().getClaimedLandSizeOutpost(village);
            int maxClaimAllowed = Utils.getChunkMaxOutpost(village.getLevel(), village.getState());
            if (amountClaimed >= maxClaimAllowed) {
                MessageUtils.tell(player, "&c&lERROR &r&7Your settlement can only claim &e{max}&r&7 outpost chunks!", "{max}", Integer.toString(maxClaimAllowed));
                return;
            }
            for (Chunk chunk : Utils.getChunksAroundPlayer(player, 4)) {
                OwnedLand checkLand = LandManager.getInstance().getLand(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
                if (checkLand == null) {
                    continue;
                }
                if (!checkLand.getOwnerVillage().equals(village.getName())) {
                    MessageUtils.tell(player, "&c&lERROR &r&7You are too close to &e{owner}'s land !", "{owner}", checkLand.getOwnerVillage());
                    return;
                }
            }
            OwnedLand land = LandManager.getInstance().getLandFromLocation(player.getLocation());
            if (land != null) {
                if (land.getOwnerVillage().equals(village.getName())) {
                    MessageUtils.tell(player, MessageUtils.ALREADY_OWN_LAND_MESSAGE, null, null);
                    return;
                }
                MessageUtils.tell(player, MessageUtils.CANT_CLAIM_HERE_MESSAGE, "{owner}", land.getOwnerVillage());
                return;
            } else {
                LandManager.getInstance().claimOutpost(player.getLocation(), village);
                MessageUtils.tellResidents(village.getResidents(), "&a&lSUCCESS &r&e{player} &7has claimed an &coutpost&7 for your settlement!", "{player}", player.getName());
            }
        }
        if (args[0].equalsIgnoreCase("unclaim")) {
            if (!VillageRank.hasRank(res.getVillageRank(), VillageRank.CHIEF)) {
                MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + "&r&c&l!");
                return;
            }
            Chunk currentChunk = player.getLocation().getChunk();
            OwnedLand land2 = LandManager.getInstance().getLand(currentChunk.getX(), currentChunk.getZ(), currentChunk.getWorld().getName());
            if (land2 == null || !land2.getOwnerVillage().equals(village.getName())) {
                MessageUtils.tell(player, "&cYou do not own this land.");
                return;
            }
            if (!land2.getIsOutpost()) {
                MessageUtils.tell(player, "&cThis land is not an outpost, please use &7/s unclaim&c to unclaim this land.");
                return;
            }
            boolean landCheck = false;
            for (Chunk chunk : Utils.getChunksAroundPlayer(player, 4)) {
                OwnedLand checkLand = LandManager.getInstance().getLand(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
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
            StringBuilder sb = new StringBuilder("&7------- &cOutPosts&7 -------\n");
            for (OwnedLand land2 : LandManager.getInstance().getClaimedLand(village)) {
                if (land2.getIsOutpost()) {
                    if (village.getOutpostHomes().containsKey(String.valueOf(land2.getX()) + "-" + land2.getWorldName() + "-" + land2.getZ())) {
                        Location loc = village.getOutpostHomes().get(String.valueOf(land2.getX()) + "-" + land2.getWorldName() + "-" + land2.getZ());
                        String name = "";
                        for (Map.Entry<String, Document> doc : village.getOutpostHomesList().entrySet()) {
                            if (doc.getValue().containsKey("name") && doc.getKey().equals(String.valueOf(land2.getX()) + "-" + land2.getWorldName() + "-" + land2.getZ())) {
                                name = doc.getValue().getString("name");
                            }
                        }
                        sb.append("&a").append(name);
                        sb.append(" (World: ");
                        sb.append(loc.getWorld().getName());
                        sb.append(", X: ");
                        sb.append(loc.getX());
                        sb.append(", Z: ");
                        sb.append(loc.getZ());
                        sb.append(" )\n");
                    } else {
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
            for (Map.Entry<String, Document> document : village.getOutpostHomesList().entrySet()) {
                if (document.getValue().containsKey("name") && document.getValue().getString("name").equalsIgnoreCase(args[1])) {
                    doc2 = document.getValue();
                    break;
                }
            }
            if (doc2 != null) {
                Location loc2 = new Location(Bukkit.getWorld(doc2.getString("worldName")), doc2.getDouble("x"), doc2.getDouble("y"), doc2.getDouble("z"));
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
            OwnedLand land3 = LandManager.getInstance().getLandFromLocation(player.getLocation());
            if (land3 == null || !land3.getIsOutpost()) {
                MessageUtils.tell(player, "&a&lLand must be an outpost to set a home here!");
                return;
            }
            village.addOutpostHome(player.getLocation(), land3, args[1]);
            VillageManager.getInstance().saveVillage(village);
            MessageUtils.tell(player, "&a&lHome &6" + args[1] + " &aset here");
        }
    }
}
