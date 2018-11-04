package me.crz.minecraft.kingdoms.command.village;

import com.avaje.ebeaninternal.server.lib.util.MailEvent;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.command.ChatCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageAccess;
import me.crz.minecraft.kingdoms.village.VillageManager;
import net.milkbowl.vault.economy.EconomyResponse;

public class VillageJoinCommand extends VillageSubCommand {
    public VillageJoinCommand(SubCommand parent) {
        super(parent, "join <name>");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Village joinVillage = VillageManager.getInstance().getVillageFromName(args[0]);
        if (joinVillage == null) {
            resident.tell(getError("village-doesnt-exist"));
        } else if (joinVillage.getAccess().below(VillageAccess.OPEN) && !Utils.playerHasInvite(resident.getUUID(), joinVillage)) {
            resident.tell(getMessage("no-invite"));
        } else if (joinVillage.isFull()) {
            resident.tell(getMessage("is-full"), "state", joinVillage.getState().getTag(), "name", joinVillage.getName());
        } else if (joinVillage.isBanished(resident.getUUID())) {
            resident.tell(getMessage("banished"), "state", joinVillage.getState().getTag(), "name", joinVillage.getName());
        } else if (joinVillage.getJoinFee() > 0 && !KingdomsPlugin.getInstance().econ.has(resident.getPlayer(), joinVillage.getJoinFee())) {
            resident.tell(getError("not-enough-money"), "state", joinVillage.getState().getTag(), "name", joinVillage.getName(), "amount", String.valueOf(joinVillage.getJoinFee()));
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
            resident.setVillageRank(VillageRank.RESIDENT);
            resident.addChannel(ChatChannel.SETTLEMENT);
            Message.get("command.chat.joined").tell(resident, "name", ChatChannel.SETTLEMENT.getTag());
            InviteCache.getPendingVillageInvites().remove(resident.getUUID(), joinVillage.getId());
            Utils.runAsync(() -> DatabaseManager.getInstance().saveResident(resident));
            resident.tell(getMessage("success"), "name", joinVillage.getName(), "state", joinVillage.getState().getTag());
            MessageUtils.tellResidents(joinVillage.getOnlineResidents(), getMessage("new-resident"), "player", resident.getName());
        }
        return true;
    }
}
