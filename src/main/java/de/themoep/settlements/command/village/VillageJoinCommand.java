package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.village.events.SettlementJoinEvent;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.rank.VillageRank;
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
        Village joinVillage = VillageManager.getInstance().getFromName(args[0]);
        if (joinVillage == null || joinVillage.isAbandoned()) {
            getError("village-doesnt-exist").tell(resident, "name", args[0]);
        } else if (joinVillage.getId().isNull()) {
            getError("missing-village-id").tell(resident, "name", joinVillage.getName());
        } else if (joinVillage.getAccess().below(LandAccess.OPEN) && !Utils.playerHasInvite(resident.getUUID(), joinVillage)) {
            getMessage("no-invite").tell(resident, "name", joinVillage.getName(), "state", joinVillage.getStateTag());
        } else if (joinVillage.isFull()) {
            getMessage("is-full").tell(resident, "state", joinVillage.getState().getTag(), "name", joinVillage.getName());
        } else if (joinVillage.isBanished(resident.getUUID())) {
            getMessage("banished").tell(resident, "state", joinVillage.getState().getTag(), "name", joinVillage.getName());
        } else if (joinVillage.getJoinFee() > 0 && !SettlementsPlugin.getInstance().getEconomy().has(resident.getPlayer(), joinVillage.getJoinFee())) {
            getError("not-enough-money").tell(resident, "state", joinVillage.getState().getTag(), "name", joinVillage.getName(), "amount", String.valueOf(joinVillage.getJoinFee()));
        } else {
            Utils.callEvent(new SettlementJoinEvent(joinVillage, resident));
            if (joinVillage.getJoinFee() > 0) {
                if (SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), joinVillage.getJoinFee()).transactionSuccess()) {
                    joinVillage.addToBank(joinVillage.getJoinFee());
                    getMessage("money-paid").tell(resident, "amount", String.valueOf(joinVillage.getJoinFee()));
                    Utils.runAsync(() -> {
                        DatabaseManager.getInstance().saveSettlement(joinVillage);
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
            DatabaseManager.getInstance().saveResident(resident);
            resident.tell(getMessage("success"), "name", joinVillage.getName(), "state", joinVillage.getState().getTag());
            getMessage("new-resident").tell(joinVillage.getOnlineResidents(), "player", resident.getName());
        }
        return true;
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (resident.hasVillage()) {
            getError("free-required").tell(resident);
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return !resident.hasVillage() && super.hasAccess(resident);
    }
}
