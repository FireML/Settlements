package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.chat.ChatChannel;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.village.VillageSubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.db.InviteCache;
import de.themoep.settlements.empire.Empire;
import de.themoep.settlements.empire.EmpireManager;
import de.themoep.settlements.land.LandAccess;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;

public class EmpireJoinCommand extends VillageSubCommand {
    public EmpireJoinCommand(SubCommand parent) {
        super(parent, "join <name>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Empire joinEmpire = EmpireManager.getInstance().getEmpireFromName(args[0]);
        if (joinEmpire == null || joinEmpire.isAbandoned()) {
            resident.tell(getError("empire-doesnt-exist"), "name", args[0]);
        } else if (joinEmpire.getId().isNull()) {
            getError("missing-empire-id").tell(resident, "name", joinEmpire.getName());
        } else if (joinEmpire.getAccess().below(LandAccess.OPEN) && !Utils.villageHasInvite(resident.getVillage(), joinEmpire)) {
            resident.tell(getMessage("no-invite"),
                    "name", joinEmpire.getName(),
                    "state", resident.getVillage().getState().getTag()
            );
        } else if (joinEmpire.isFull()) {
            resident.tell(getMessage("is-full"),
                    "name", joinEmpire.getName(),
                    "state", resident.getVillage().getState().getTag()
            );
        } else if (joinEmpire.getJoinFee() > 0 && resident.getVillage().getBank() < joinEmpire.getJoinFee()) {
            resident.tell(getError("not-enough-money"),
                    "name", joinEmpire.getName(),
                    "amount", String.valueOf(joinEmpire.getJoinFee()),
                    "state", resident.getVillage().getState().getTag()
            );
        } else {
            if (joinEmpire.getJoinFee() > 0) {
                resident.getVillage().removeFromBank(joinEmpire.getJoinFee());
                joinEmpire.addToBank(joinEmpire.getJoinFee());
                getMessage("money-paid").tell(resident, "amount", String.valueOf(joinEmpire.getJoinFee()));
                Utils.runAsync(() -> {
                    DatabaseManager.getInstance().saveEmpire(joinEmpire);
                    DatabaseManager.getInstance().log(joinEmpire, resident, "joinfee", "cash", joinEmpire.getJoinFee());
                });
            }
            resident.getVillage().setEmpire(joinEmpire);
            if (Config.Empire.Missions.DISTRIBUTE_ON_JOIN) {
                joinEmpire.distributeNewMissions(resident.getVillage());
            }
            for (Resident member : resident.getVillage().getOnlineResidents()) {
                member.addChannel(ChatChannel.EMPIRE);
            }
            InviteCache.getPendingEmpireInvites().remove(resident.getVillage().getId(), joinEmpire.getId());
            DatabaseManager.getInstance().saveSettlement(resident.getVillage());
            DatabaseManager.getInstance().saveMissionData(resident.getVillage());
            getMessage("success").tell(resident.getVillage().getOnlineResidents(),
                    "name", joinEmpire.getName(),
                    "state", resident.getVillage().getState().getTag()
            );
            MessageUtils.tellResidents(joinEmpire.getOnlineResidents(), getMessage("new-member"),
                    "name", resident.getVillage().getName(),
                    "state", resident.getVillage().getState().getTag()
            );
        }
        return true;
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (resident.hasEmpire()) {
            getError("no-empire-required").tell(resident);
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return !resident.hasEmpire() && super.hasAccess(resident);
    }
}
