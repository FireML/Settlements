package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.OfflinePlayer;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import org.bson.Document;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.TownCache;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageKickCommand extends SubCommand {
    public VillageKickCommand() {
        super("Kick a player from your village", "kick");
        this.setPermission("village.user");
        this.setUsage("kick <name>");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        if (!Utils.playerCanKick(player, village)) {
            MessageUtils.tell(player, MessageUtils.NO_PERMISSION_MESSAGE, null, null);
            return;
        }
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        final OfflinePlayer kickedPlayer = Bukkit.getOfflinePlayer(args[0]);
        if (kickedPlayer == null) {
            MessageUtils.tell(player, MessageUtils.PLAYER_NOT_FOUND_MESSAGE, null, null);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(kickedPlayer.getUniqueId());
        final Village playerVillage = VillageManager.getInstance().getVillageFromResident(res);
        if (!Utils.playerInSameVillage(village, playerVillage)) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        if (kickedPlayer.getUniqueId().equals(player.getUniqueId())) {
            MessageUtils.tell(player, "&cYou cannot kick yourself!", null, null);
            return;
        }
        if (playerVillage.getChief().equals(player.getUniqueId())) {
            MessageUtils.tell(player, "&cYou cannot kick the &6" + res.getVillageRank().getTag(playerVillage.getState()) + "&c!", null, null);
            return;
        }
        MessageUtils.tellResidents(village.getResidents(), MessageUtils.KICKED_PLAYER_MESSAGE, "{player}", kickedPlayer.getName());
        res.setVillageName("");
        res.setVillageRank(VillageRank.RESIDENT);
        ResidentManager.getInstance().saveResident(res);
        if (TownCache.getPendingTownInvites().get(res.getUUID()).equals(playerVillage.getName())) {
            TownCache.getPendingTownInvites().remove(res.getUUID());
        }
        for (final Document doc : DatabaseManager.getLandCollection().find(new Document("plotOwner", res.getUUID().toString()))) {
            final OwnedLand land = new OwnedLand(doc);
            land.setPlotOwner(null);
            LandManager.getInstance().saveLand(land);
        }
    }
}
