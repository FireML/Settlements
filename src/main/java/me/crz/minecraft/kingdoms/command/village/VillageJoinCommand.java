package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class VillageJoinCommand extends ResidentSubCommand {
    public VillageJoinCommand(SubCommand parent) {
        super(parent, "join <name>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.hasVillage()) {
            getError("free-required").tell(resident);
            return true;
        }
        if (args.length < 1) {
            return false;
        }
        Village joinVillage = VillageManager.getInstance().getVillageFromName(args[0]);
        if (joinVillage == null) {
            getError("village-doesnt-exist").tell(resident, "name", args[0]);
        } else if (joinVillage.getId().isNull()) {
            getError("missing-village-id").tell(resident, "name", joinVillage.getName());
        } else if (joinVillage.getAccess().below(LandAccess.OPEN) && !Utils.playerHasInvite(resident.getUUID(), joinVillage)) {
            getMessage("no-invite").tell(resident, "name", joinVillage.getName(), "state", joinVillage.getStateTag());
        } else if (joinVillage.isFull()) {
            getMessage("is-full").tell(resident, "state", joinVillage.getState().getTag(), "name", joinVillage.getName());
        } else if (joinVillage.isBanished(resident.getUUID())) {
            getMessage("banished").tell(resident, "state", joinVillage.getState().getTag(), "name", joinVillage.getName());
        } else if (joinVillage.getJoinFee() > 0 && !KingdomsPlugin.getInstance().econ.has(resident.getPlayer(), joinVillage.getJoinFee())) {
            getError("not-enough-money").tell(resident, "state", joinVillage.getState().getTag(), "name", joinVillage.getName(), "amount", String.valueOf(joinVillage.getJoinFee()));
        } else {
            if (joinVillage.getJoinFee() > 0) {
                if (KingdomsPlugin.getInstance().econ.withdrawPlayer(resident.getPlayer(), joinVillage.getJoinFee()).transactionSuccess()) {
                    joinVillage.addToBank(joinVillage.getJoinFee());
                    getMessage("money-paid").tell(resident, "amount", String.valueOf(joinVillage.getJoinFee()));
                    Utils.runAsync(() -> {
                        DatabaseManager.getInstance().saveVillage(joinVillage);
                        DatabaseManager.getInstance().log(joinVillage, resident, "joinfee", "cash", joinVillage.getJoinFee());
                    });
                } else {
                    resident.tell("&cAn internal error occured!");
                    return true;
                }
            }
            resident.setVillage(joinVillage);
            resident.setVillageRank(VillageRank.getDefault());
            resident.addChannel(ChatChannel.SETTLEMENT);
            InviteCache.getPendingVillageInvites().remove(resident.getUUID(), joinVillage.getId());
            Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(resident));
            resident.tell(getMessage("success"), "name", joinVillage.getName(), "state", joinVillage.getState().getTag());
            getMessage("new-resident").tell(joinVillage.getOnlineResidents(), "player", resident.getName());
        }
        return true;
    }
}
