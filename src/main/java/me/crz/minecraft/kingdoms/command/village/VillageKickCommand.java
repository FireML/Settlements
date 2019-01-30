package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;

public class VillageKickCommand extends VillageSubCommand {
    public VillageKickCommand(SubCommand parent) {
        super(parent, "kick <name>");
    }

    @Override
    public boolean run(Resident player, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        if (args[0].equalsIgnoreCase(player.getName())) {
            MessageUtils.tell(player, getMessage("cant-kick-yourself"));
            return true;
        }
        Utils.runAsync(() -> {
            Resident res = ResidentManager.getInstance().getResident(args[0]);
            if (res == null) {
                MessageUtils.tell(player, getError("player-not-found"));
                return;
            }
            if (!player.getVillage().equals(res.getVillage())) {
                MessageUtils.tell(player, getError("not-in-current"));
                return;
            }
            if (res.getVillageRank().atLeast(player.getVillageRank())) {
                MessageUtils.tell(player, getMessage("cant-kick-higher"), "rank", res.getVillageRank().getTag(res.getVillage()));
                return;
            }
            MessageUtils.tellResidents(res.getVillage().getOnlineResidents(), getMessage("kicked-alert"), "player", res.getName());
            Utils.runSync(() -> {
                InviteCache.getPendingVillageInvites().remove(res.getUUID(), res.getVillage().getId());
                res.leaveVillage();
            });
        });
        return true;
    }
}
