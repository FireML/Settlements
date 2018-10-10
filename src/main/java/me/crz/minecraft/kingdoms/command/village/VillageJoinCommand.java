package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.TownCache;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageJoinCommand extends SubCommand {
    public VillageJoinCommand() {
        super("Join a village", "join");
        this.setPermission("kingdoms.user");
        this.setUsage("join <name>");
    }

    @Override
    public void run(Player player, String base, String alias, String... args) {
        if (args.length < 1) {
            MessageUtils.tell(player, "&c" + this.getUsage(base));
            return;
        }
        Village joinVillage = VillageManager.getInstance().getVillageFromName(args[0]);
        if (joinVillage == null) {
            MessageUtils.tell(player, MessageUtils.Message.TOWN_NOT_FOUND);
            return;
        }
        if (joinVillage.getAccess().below(VillageAccess.OPEN) && !Utils.playerHasInvite(player, joinVillage.getName())) {
            MessageUtils.tell(player, MessageUtils.Message.NO_INVITE);
            return;
        }
        if (joinVillage.isFull()) {
            MessageUtils.tell(player, MessageUtils.Message.IS_FULL);
            return;
        }
        Resident res = ResidentManager.getInstance().getResident(player);
        if (res.getVillage() != null) {
            MessageUtils.tell(player, MessageUtils.Message.MUST_LEAVE_CURRENT);
            return;
        }
        res.setVillage(joinVillage);
        res.setVillageRank(VillageRank.RESIDENT);
        res.addChannel(ChatChannel.SETTLEMENT);
        TownCache.getPendingTownInvites().remove(player.getUniqueId(), joinVillage.getName());
        Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(res));
        MessageUtils.tell(player, MessageUtils.Message.SUCCESS_JOIN, "{townName}", joinVillage.getName());
        MessageUtils.tellResidents(joinVillage.getOnlineResidents(), MessageUtils.Message.NEW_RESIDENT, "{player}", player.getName());
    }
}
