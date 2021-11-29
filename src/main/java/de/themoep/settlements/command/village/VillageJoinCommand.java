package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Settlement;
import de.themoep.settlements.village.SettlementManager;
import de.themoep.settlements.village.events.SettlementJoinEvent;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.utils.Utils;

public class VillageJoinCommand extends ResidentSubCommand {
    public VillageJoinCommand(SubCommand parent) {
        super(parent, "join <name>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Settlement joinSettlement = SettlementManager.getInstance().getFromName(args[0]);
        if (joinSettlement == null || joinSettlement.isAbandoned()) {
            getError("settlement-doesnt-exist").tell(resident, "name", args[0]);
        } else if (joinSettlement.getId().isNull()) {
            getError("missing-settlement-id").tell(resident, "name", joinSettlement.getName());
        } else if (joinSettlement.getAccess().below(LandAccess.OPEN) && !Utils.playerHasInvite(resident.getUUID(), joinSettlement)) {
            getMessage("no-invite").tell(resident, "name", joinSettlement.getName(), "state", joinSettlement.getStateTag());
        } else if (joinSettlement.isFull()) {
            getMessage("is-full").tell(resident, "state", joinSettlement.getState().getTag(), "name", joinSettlement.getName());
        } else if (joinSettlement.isBanished(resident.getUUID())) {
            getMessage("banished").tell(resident, "state", joinSettlement.getState().getTag(), "name", joinSettlement.getName());
        } else if (joinSettlement.getJoinFee() > 0 && !SettlementsPlugin.getInstance().getEconomy().has(resident.getPlayer(), joinSettlement.getJoinFee())) {
            getError("not-enough-money").tell(resident, "state", joinSettlement.getState().getTag(), "name", joinSettlement.getName(), "amount", String.valueOf(joinSettlement.getJoinFee()));
        } else {
            Utils.callEvent(new SettlementJoinEvent(joinSettlement, resident));
            if (joinSettlement.getJoinFee() > 0) {
                if (SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), joinSettlement.getJoinFee()).transactionSuccess()) {
                    joinSettlement.addToBank(joinSettlement.getJoinFee());
                    getMessage("money-paid").tell(resident, "amount", String.valueOf(joinSettlement.getJoinFee()));
                    DatabaseManager.getInstance().saveSettlement(joinSettlement);
                    DatabaseManager.getInstance().log(joinSettlement, resident, "joinfee", "cash", joinSettlement.getJoinFee());
                } else {
                    resident.tell("&cAn internal error occured!");
                    return true;
                }
            }
            resident.setSettlement(joinSettlement);
            resident.setSettlementRank(SettlementRank.getDefault());
            resident.addChannel(ChatChannel.SETTLEMENT);
            InviteCache.getPendingVillageInvites().remove(resident.getUUID(), joinSettlement.getId());
            DatabaseManager.getInstance().saveResident(resident);
            resident.tell(getMessage("success"), "name", joinSettlement.getName(), "state", joinSettlement.getState().getTag());
            getMessage("new-resident").tell(joinSettlement.getOnlineResidents(), "player", resident.getName());
        }
        return true;
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (resident.hasSettlement()) {
            getError("free-required").tell(resident);
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return !resident.hasSettlement() && super.hasAccess(resident);
    }
}
