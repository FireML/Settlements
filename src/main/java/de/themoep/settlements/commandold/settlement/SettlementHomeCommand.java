package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.ResidentSubCommand;
import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.rank.SettlementRank;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.utils.Utils;

public class SettlementHomeCommand extends ResidentSubCommand {
    public SettlementHomeCommand(SubCommand parent) {
        super(parent, "home [<name>]");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Settlement settlement;
        if (args.length < 1) {
            if (!resident.hasSettlement()) {
                getError("settlement-required").tell(resident);
                return false;
            } else if (resident.getSettlement().getHome() == null) {
                getMessage("own-no-home").tell(resident, "chief", SettlementRank.getChief().getTag(resident.getSettlement()));
                return true;
            } else {
                settlement = resident.getSettlement();
            }
        } else {
            String name = String.join(" ", args);
            settlement = SettlementManager.getInstance().getFromName(name);
            if (settlement == null) {
                getError("settlement-doesnt-exist").tell(resident, "name", name);
                return true;
            }
        }

        if (!resident.getPlayer().isOp() && !resident.hasPermission("settlements.bypass.teleport") && !settlement.isMember(resident)
                && (settlement.getAccess() == LandAccess.PRIVATE
                || (settlement.getAccess() == LandAccess.ALLY && !settlement.isAlly(resident)))) {
            getMessage("no-access").tell(resident, "state", settlement.getStateTag());
            return true;
        }

        if (settlement.getAccess() != LandAccess.PUBLIC && settlement.isMember(resident)
                && resident.getSettlementRank() == resident.getSettlement().getDefaultRank()
                && !resident.getSettlement().getEffectivePermissionValue(resident.getSettlementRank(), "command.home").orElse(false)) {
            getError("rules-not-accepted").tell(resident, "state", resident.getSettlement().getStateTag(), "name", resident.getSettlement().getName());
            return true;
        }

        if (settlement.getHome() != null) {
            Utils.teleport(this, resident.getPlayer(), settlement.getHome().toBukkitLocation(), settlement.getStateKey(),
                    "state", settlement.getStateTag(), "name", settlement.getName());
        } else {
            getMessage("no-home").tell(resident, "state", settlement.getStateTag());
        }
        return true;
    }
}
