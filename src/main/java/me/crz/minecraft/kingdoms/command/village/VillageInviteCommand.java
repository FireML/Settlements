package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.village.Resident;
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

public class VillageInviteCommand extends SubCommand
{
    public VillageInviteCommand() {
        super("Invite someone to join your village", new String[] { "invite" });
        this.setPermission("village.user");
        this.setUsage("invite <player>");
    }
    
    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village == null) {
            MessageUtils.tell(player, MessageUtils.NOT_IN_CURRENT_MESSAGE, null, null);
            return;
        }
        if (!this.canInvite(ResidentManager.getInstance().getResident(player))) {
            MessageUtils.tell(player, "&c&lYou require the rank &r&6&l" + VillageRank.RECRUITER.getTag(village.getState()) + " or higher&r&c&l!", null, null);
            return;
        }
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        final Player invitedPlayer = Bukkit.getPlayerExact(args[0]);
        if (invitedPlayer == null) {
            MessageUtils.tell(player, MessageUtils.PLAYER_NOT_FOUND_MESSAGE, null, null);
            return;
        }
        final Village playerVillage = VillageManager.getInstance().getVillageFromPlayer(invitedPlayer);
        if (Utils.playerInSameVillage(village, playerVillage)) {
            MessageUtils.tell(player, MessageUtils.MUST_LEAVE_CURRENT_MESSAGE, null, null);
            return;
        }
        TownCache.getPendingTownInvites().put(invitedPlayer.getUniqueId(), village.getName());
        MessageUtils.tell(invitedPlayer, MessageUtils.RECEIVE_INVITE_MESSAGE, "{townName}", village.getName());
        MessageUtils.tellResidents(village.getResidents(), MessageUtils.SUCCESS_INVITE_MESSAGE, "{player}", invitedPlayer.getName());
    }
    
    private boolean canInvite(final Resident res) {
        return VillageRank.hasRank(res.getVillageRank(), VillageRank.RECRUITER);
    }
}
