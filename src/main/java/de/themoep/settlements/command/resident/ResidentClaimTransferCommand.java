package de.themoep.settlements.command.resident;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.VillageManager;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

public class ResidentClaimTransferCommand extends ResidentSubCommand {
    public ResidentClaimTransferCommand(SubCommand parent) {
        super(parent, "claimtransfer <settlement>|empire:<name> <amount>", "transferclaims", "bonustransfer", "transferbonus");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        int amountIndex = 1;
        LandOwner target;
        if (args.length < 2) {
            if (args.length == 0) {
                return false;
            }
            amountIndex = 0;
            if (resident.hasVillage()) {
                target = resident.getVillage();
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
            } else if (args[0].startsWith("village:") || args[0].startsWith("v:") || args[0].startsWith("settlement:") || args[0].startsWith("s:")) {
                target = VillageManager.getInstance().getFromName(args[0].split(":")[1]);
                if (target == null) {
                    getError("village-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                    return false;
                }
            } else {
                target = VillageManager.getInstance().getFromName(args[0]);
                if (target == null) {
                    getError("village-doesnt-exist").tell(resident, "name", args[0]);
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
                        "rank", resident.getVillageRank().getTag(resident.getVillage()),
                        "title", resident.getVillageTitle(),
                        "name", resident.getName(),
                        "amount", String.valueOf(transfer),
                        "target", target.getName(),
                        "targetstate", target.getStateTag());
                if (!target.isMember(resident)) {
                    getMessage("success").tell(resident,
                            "rank", resident.getVillageRank().getTag(resident.getVillage()),
                            "title", resident.getVillageTitle(),
                            "name", resident.getName(),
                            "amount", String.valueOf(transfer),
                            "target", target.getName(),
                            "targetstate", target.getStateTag());
                }
            } else {
                resident.tell(getError("not-enough-bonus"), "amount", String.valueOf(transfer));
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
