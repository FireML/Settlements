package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.db.TownCache;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageInviteCommand extends SubCommand {
    public VillageInviteCommand() {
        super("Invite someone to join your village", "invite");
        this.setPermission("kingdoms.user");
        this.setUsage("invite <player>");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, Message.get("not-in-current"), null, null);
            return;
        }
        if (ResidentManager.getInstance().getResident(player).getVillageRank().below(VillageRank.RECRUITER)) {
            MessageUtils.tell(player, Message.get("rank-required"), "rank", VillageRank.RECRUITER.getTag(village.getState()));
            return;
        }
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        final Player invitedPlayer = Bukkit.getPlayerExact(args[0]);
        if (invitedPlayer == null) {
            MessageUtils.tell(player, Message.get("player-not-found"), null, null);
            return;
        }
        final Village playerVillage = VillageManager.getInstance().getVillageFromPlayer(invitedPlayer);
        if (Utils.playerInSameVillage(village, playerVillage)) {
            MessageUtils.tell(player, Message.get("must-leave-current"), null, null);
            return;
        }
        TownCache.getPendingTownInvites().put(invitedPlayer.getUniqueId(), village.getName());
        MessageUtils.tell(invitedPlayer, Message.get("receive-invite"), "{townName}", village.getName());
        MessageUtils.tellResidents(village.getOnlineResidents(), Message.get("success-invite"), "{player}", invitedPlayer.getName());
    }
}
