package me.crz.minecraft.kingdoms.command.resident;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.utils.BonusClaimsHolder;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.entity.Player;

public class ResidentClaimTransferCommand extends SubCommand {
    public ResidentClaimTransferCommand(SubCommand parent) {
        super(parent, "claimtransfer <settlement>|empire:<name> <amount>", "transferclaims", "bonustransfer", "transferbonus");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        Resident resident = ResidentManager.getInstance().getResident(player);
        int amountIndex = 1;
        BonusClaimsHolder target;
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
            if (args[0].startsWith("empire:") || args[0].startsWith("e:")) {
                target = EmpireManager.getInstance().getEmpireFromName(args[0].split(":")[1]);
                if (target == null) {
                    getError("empire-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                    return false;
                }
            } else if (args[0].startsWith("village:") || args[0].startsWith("v:") || args[0].startsWith("settlement:") || args[0].startsWith("s:")) {
                target = VillageManager.getInstance().getVillageFromName(args[0].split(":")[1]);
                if (target == null) {
                    getError("village-doesnt-exist").tell(resident, "name", args[0].split(":")[1]);
                    return false;
                }
            } else {
                target = VillageManager.getInstance().getVillageFromName(args[0]);
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
                Utils.runAsync(() -> {
                    DatabaseManager.getInstance().saveResident(resident);
                    if (target instanceof Village) {
                        DatabaseManager.getInstance().saveVillage((Village) target);
                    } else if (target instanceof Empire) {
                        DatabaseManager.getInstance().saveEmpire((Empire) target);
                    }
                    DatabaseManager.getInstance().log(resident.getVillage(), resident, "deposit", "claim", transfer);
                });
                getMessage("success").tell(target.getOnlineResidents(),
                        "rank", resident.getVillageRank().getTag(resident.getVillage()),
                        "title", resident.getTitle(),
                        "name", resident.getName(),
                        "amount", String.valueOf(transfer),
                        "state", resident.getVillage().getState().getTag());
                if (!target.isMember(resident)) {
                    getMessage("success").tell(resident,
                            "rank", resident.getVillageRank().getTag(resident.getVillage()),
                            "title", resident.getTitle(),
                            "name", resident.getName(),
                            "amount", String.valueOf(transfer),
                            "state", resident.getVillage().getState().getTag());
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
