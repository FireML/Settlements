package de.themoep.settlements.command.resident;

import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

public class ResidentClaimTransferCommand extends ResidentSubCommand {
    public ResidentClaimTransferCommand(SubCommand parent) {
        super(parent, "claimtransfer <settlement>|empire:<name> <amount>", "transferclaims", "bonustransfer", "transferbonus");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        int amountIndex = 1;
        LandOwner<?, ?> target;
        if (args.length < 2) {
            if (args.length == 0) {
                return false;
            }
            amountIndex = 0;
            if (resident.hasSettlement()) {
                target = resident.getSettlement();
            } else {
                return false;
            }
        } else {
            if ((args[0].startsWith("empire:") || args[0].startsWith("e:")) && Config.Empire.CLAIM_TRANSFER) {
                target = EmpireManager.getInstance().getEmpireFromName(args[0].split(":")[1]);
                if (target == null) {
                    getError("empire-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                    return false;
                }
            } else if (args[0].startsWith("settlement:") || args[0].startsWith("s:")) {
                target = SettlementManager.getInstance().getFromName(args[0].split(":")[1]);
                if (target == null) {
                    getError("settlement-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                    return false;
                }
            } else {
                target = SettlementManager.getInstance().getFromName(args[0]);
                if (target == null) {
                    getError("settlement-doesnt-exist").tell(resident, "name", args[0]);
                    return false;
                }
            }
        }

        try {
            int transfer = Integer.parseInt(args[amountIndex]);
            if (resident.removeBonusClaims(transfer)) {
                target.addBonusClaims(transfer);
                DatabaseManager.getInstance().saveResident(resident);
                DatabaseManager.getInstance().save(target);
                Utils.runAsync(() -> DatabaseManager.getInstance().log(target, resident, "deposit", "claim", transfer));
                getMessage("success").tell(target.getOnlineResidents(),
                        "rank", resident.getSettlementRank().getTag(resident.getSettlement()),
                        "title", resident.getSettlementTitle(),
                        "name", resident.getName(),
                        "amount", String.valueOf(transfer),
                        "target", target.getName(),
                        "targetstate", target.getStateTag());
                if (!target.isMember(resident)) {
                    getMessage("success").tell(resident,
                            "rank", resident.getSettlementRank().getTag(resident.getSettlement()),
                            "title", resident.getSettlementTitle(),
                            "name", resident.getName(),
                            "amount", String.valueOf(transfer),
                            "target", target.getName(),
                            "targetstate", target.getStateTag());
                }
            } else {
                resident.tell(getError("not-enough-bonus").getText(), "amount", String.valueOf(transfer));
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
