package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.utils.ExecuteMoveUtil;
import me.crz.minecraft.kingdoms.world.NamedLocation;

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

import java.text.DecimalFormat;
import java.util.Collection;

public class VillageOutPostCommand extends SubCommand {
    private StringBuilder sb;
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.#");

    public VillageOutPostCommand() {
        super("Claim a seperate chunk of land", "outpost");
        sb = new StringBuilder();
        this.setPermission("kingdoms.user");
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
            MessageUtils.tell(player, MessageUtils.Message.NOT_IN_CURRENT);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (args[0].equalsIgnoreCase("claim")) {
            if (res.getVillageRank().below(VillageRank.ELDER)) {
                MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.ELDER.getTag(village.getState()));
                return;
            }
            if (!Utils.isWorldClaimable(player.getWorld())) {
                MessageUtils.tell(player, "&c&lClaiming is disabled in this world!", null, null);
                return;
            }
            if (args.length < 2) {
                MessageUtils.tell(player, "/s outpost claim <name>");
                return;
            }
            int amountClaimed = village.getOutpostHomes().size();
            int maxClaimAllowed = Utils.getChunkMaxOutpost(village.getLevel(), village.getState());
            if (amountClaimed >= maxClaimAllowed) {
                MessageUtils.tell(player, MessageUtils.Message.ERROR_MAX_OUTPOST_CHUNKS, "{max}", Integer.toString(maxClaimAllowed));
                return;
            }
            for (Chunk chunk : Utils.getChunksAroundPlayer(player, 4)) {
                OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
                if (checkLand == null) {
                    continue;
                }
                if (!checkLand.getOwnerVillage().equals(village)) {
                    MessageUtils.tell(player, "&c&lERROR &r&7You are too close to &e{owner}'s land !", "{owner}", checkLand.getOwnerVillage().getName());
                    return;
                }
            }
            OwnedLand land = LandManager.getInstance().getLand(player.getLocation());
            if (land != null) {
                if (land.getOwnerVillage().equals(village)) {
                    MessageUtils.tell(player, MessageUtils.Message.ALREADY_OWN_LAND, null, null);
                    return;
                }
                MessageUtils.tell(player, MessageUtils.Message.CANT_CLAIM_HERE, "{owner}", land.getOwnerVillage().getName());
                return;
            } else {
                OwnedLand outpost = LandManager.getInstance().claimOutpost(player.getLocation(), village, args[1]);
                MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.SUCCESS_CLAIM_OUTPOST,
                        "player", player.getName(),
                        "name", args[1],
                        "x", String.valueOf(player.getLocation().getBlockX()),
                        "z", String.valueOf(player.getLocation().getBlockZ()));
                Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(outpost));
            }
        } else if (args[0].equalsIgnoreCase("unclaim")) {
            if (res.getVillageRank().below(VillageRank.CHIEF)) {
                MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.CHIEF.getTag(village.getState()) + "&r&c&l!");
                return;
            }
            Chunk currentChunk = player.getLocation().getChunk();
            OwnedLand land = LandManager.getInstance().getLand(currentChunk);
            if (land == null || !land.getOwnerVillage().equals(village)) {
                MessageUtils.tell(player, "&cYou do not own this land.");
                return;
            }
            if (!land.getIsOutpost()) {
                MessageUtils.tell(player, "&cThis land is not an outpost, please use &7/s unclaim&c to unclaim this land.");
                return;
            }

            Collection<OwnedLand> toUnclaim = LandManager.getInstance().getAllConnectedLand(land, true);
            toUnclaim.add(land);
            LandManager.getInstance().unclaimLand(toUnclaim);
            MessageUtils.tellResidents(village.getOnlineResidents(), MessageUtils.Message.SUCCESS_UNCLAIM_OUTPOST,
                    "amount", String.valueOf(toUnclaim.size() - 1),
                    "x", String.valueOf(player.getLocation().getBlockX()),
                    "z", String.valueOf(player.getLocation().getBlockZ()));
        } else if (args[0].equalsIgnoreCase("list")) {
            StringBuilder sb = new StringBuilder("&7------- &cOutPosts&7 -------\n");
            for (NamedLocation loc : village.getOutpostHomes().values()) {
                sb.append("&a")
                        .append(loc.getName())
                        .append(" (World: ")
                        .append(loc.getWorld())
                        .append(", X: ")
                        .append(DOUBLE_FORMAT.format(loc.getX()))
                        .append(", Z: ")
                        .append(DOUBLE_FORMAT.format(loc.getZ()))
                        .append(" )\n");
            }
            MessageUtils.tell(player, sb.toString());
        } else if (args[0].equalsIgnoreCase("tp")) {
            if (args.length < 2) {
                MessageUtils.tell(player, "/s outpost tp <name>");
                return;
            }
            NamedLocation location = village.getOutpostHomes().get(args[1].toLowerCase());
            if (location != null) {
                Location loc2 = location.toBukkitLocation();
                MessageUtils.tell(player, "&e&lTeleporting in 5 seconds...");
                ExecuteMoveUtil.getInstance().execute(player, () -> player.teleport(loc2), 5, "Teleport");
                return;
            }
            MessageUtils.tell(player, MessageUtils.Message.OUTPOST_NOT_FOUND, "name", args[1]);
        } else if (args[0].equalsIgnoreCase("sethome")) {
            if (res.getVillageRank().below(VillageRank.ELDER)) {
                MessageUtils.tell(player, MessageUtils.Message.RANK_REQUIRED, "rank", VillageRank.ELDER.getTag(village.getState()));
                return;
            }
            if (args.length < 2) {
                MessageUtils.tell(player, "/s outpost sethome <name>");
                return;
            }
            OwnedLand land3 = LandManager.getInstance().getLand(player.getLocation());
            if (land3 == null || !land3.getIsOutpost()) {
                MessageUtils.tell(player, "&a&lLand must be an outpost to set a home here!");
                return;
            }
            village.addOutpostHome(args[1], player.getLocation());
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(village));
            MessageUtils.tell(player, "&a&lHome &6" + args[1] + " &aset here");
        }
    }
}
