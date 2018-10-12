package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.db.TownCache;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageKickCommand extends SubCommand {
    public VillageKickCommand() {
        super("Kick a player from your village", "kick");
        this.setPermission("kingdoms.user");
        this.setUsage("kick <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"), null, null);
            return;
        }
        if (!Utils.playerCanKick(player, village)) {
            MessageUtils.tell(player, Message.get("no-permission"), null, null);
            return;
        }
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        if (args[0].equalsIgnoreCase(player.getName())) {
            MessageUtils.tell(player, "&cYou cannot kick yourself!", null, null);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(args[0]);
        if (res == null) {
            MessageUtils.tell(player, Message.get("player-not-found"), null, null);
            return;
        }
        Village playerVillage = VillageManager.getInstance().getVillageFromResident(res);
        if (!Utils.playerInSameVillage(village, playerVillage)) {
            MessageUtils.tell(player, Message.get("not-in-current"), null, null);
            return;
        }
        if (playerVillage.getChief().equals(player.getUniqueId())) {
            MessageUtils.tell(player, "&cYou cannot kick the &6" + res.getVillageRank().getTag(playerVillage.getState()) + "&c!", null, null);
            return;
        }
        MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("kicked-player"), "{player}", res.getName());
        TownCache.getPendingTownInvites().remove(res.getUUID(), playerVillage.getName());
        VillageManager.getInstance().leaveVillage(res);
    }
}
