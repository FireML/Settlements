package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;

public class VillageInviteCommand extends VillageSubCommand {
    public VillageInviteCommand(SubCommand parent) {
        super(parent, "invite <player>", VillageRank.RECRUITER);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Utils.runAsync(() -> {
            Resident invitedPlayer = ResidentManager.getInstance().getResident(args[0]);
            if (invitedPlayer == null) {
                res.tell(getError("player-not-found"), "name", args[0]);
                return;
            }
            if (invitedPlayer.hasVillage()) {
                res.tell(getMessage("already-in-village"), "name", invitedPlayer.getName(), "state", res.getVillage().getState().getTag());
                return;
            }
            Utils.runSync(() -> {
                InviteCache.getPendingVillageInvites().put(invitedPlayer.getUUID(), res.getVillage().getId());
                getMessage("received").tell(invitedPlayer, "name", res.getVillage().getName(), "state", res.getVillage().getState().getTag());
                getMessage("joinfee").tell(invitedPlayer, "name", res.getVillage().getName(), "state", res.getVillage().getState().getTag(), "amount", String.valueOf(res.getVillage().getJoinFee()));
            });
            getMessage("alert").tell(res.getVillage().getOnlineResidents(), "player", invitedPlayer.getName());
        });
        return true;
    }
}
