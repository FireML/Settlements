package de.themoep.settlements.command.settlement;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.invites.InviteManager;
import de.themoep.settlements.land.JoinRequest;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.JoinType;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.MessageUtils;

import java.util.stream.Collectors;

public class SettlementJoinCommand extends ResidentSubCommand {
    public SettlementJoinCommand(SubCommand parent) {
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
        } else if (joinSettlement.isFull()) {
            getMessage("is-full").tell(resident, "state", joinSettlement.getState().getTag(), "name", joinSettlement.getName());
        } else if (joinSettlement.isBanished(resident)) {
            getMessage("banished").tell(resident, "state", joinSettlement.getState().getTag(), "name", joinSettlement.getName());
        } else if (joinSettlement.getJoinFee() > 0 && !SettlementsPlugin.getInstance().getEconomy().has(resident.getPlayer(), joinSettlement.getJoinFee())) {
            getError("not-enough-money").tell(resident, "state", joinSettlement.getState().getTag(), "name", joinSettlement.getName(), "amount", String.valueOf(joinSettlement.getJoinFee()));
        } else if (joinSettlement.getJoinType() != JoinType.ANYONE && InviteManager.getInstance().getInvite(resident, joinSettlement) == null) {
            if (joinSettlement.getJoinType() == JoinType.APPLY) {
                if (joinSettlement.hasUnlocked("recruitment") && joinSettlement.hasUnlocked("command.recruitment")) {
                    getMessage("applied").tell(resident, "name", joinSettlement.getName(), "state", joinSettlement.getStateTag());
                    JoinRequest request = new JoinRequest(joinSettlement, resident);
                    joinSettlement.addJoinRequest(request);
                    request.save();
                    MessageUtils.notify(
                            joinSettlement,
                            joinSettlement.getResidents().stream().filter(r -> r.hasPermission("settlement.recruitment")).collect(Collectors.toList()),
                            Message.get("command.settlement.join.someone-applied"),
                            "name", resident.getName(),
                            "state", joinSettlement.getStateTag()
                    );
                } else {
                    resident.tell(getUnlockText(joinSettlement, "recruitment"));
                    getMessage("no-invite").tell(resident, "name", joinSettlement.getName(), "state", joinSettlement.getStateTag());
                }
            } else {
                getMessage("no-invite").tell(resident, "name", joinSettlement.getName(), "state", joinSettlement.getStateTag());
            }
        } else {
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
            joinSettlement.addMember(resident);
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
