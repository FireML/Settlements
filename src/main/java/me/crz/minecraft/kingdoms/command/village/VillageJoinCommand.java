package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageJoinCommand extends SubCommand {
    public VillageJoinCommand() {
        super("Join a village", "join");
        this.setPermission("village.user");
        this.setUsage("join <name>");
    }

    @Override
    public void run(final Player player, final String base, final String alias, final String... args) {
        final Village village = VillageManager.getInstance().getVillageFromPlayer(player);
        if (village != null) {
            MessageUtils.tell(player, MessageUtils.MUST_LEAVE_CURRENT_MESSAGE);
            return;
        }
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        final Village joinVillage = VillageManager.getInstance().getVillageFromName(args[0].toLowerCase());
        if (joinVillage == null) {
            MessageUtils.tell(player, MessageUtils.TOWN_NOT_FOUND_MESSAGE);
            return;
        }
        if (!Utils.playerHasInvite(player, joinVillage.getName())) {
            MessageUtils.tell(player, MessageUtils.NO_INVITE_MESSAGE);
            return;
        }
        if (joinVillage.isFull()) {
            MessageUtils.tell(player, MessageUtils.IS_FULL_MESSAGE);
            return;
        }
        final Resident res = ResidentManager.getInstance().getResident(player);
        res.setVillageName(joinVillage.getName());
        res.setVillageRank(VillageRank.RESIDENT);
        res.addChannel(ChatChannel.SETTLEMENT);
        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
        MessageUtils.tell(player, MessageUtils.SUCCESS_JOIN_MESSAGE, "{townName}", joinVillage.getName());
        MessageUtils.tellResidents(joinVillage.getResidents(), MessageUtils.NEW_RESIDENT_MESSAGE, "{player}", player.getName());
    }
}
