package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.chat.ChatChannel;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.empire.Empire;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.land.LandAccess;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireJoinCommand extends EmpireSubCommand {
    public EmpireJoinCommand(SubCommand parent) {
        super(parent, "join <name>");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Empire joinEmpire = EmpireManager.getInstance().getEmpireFromName(args[0]);
        if (joinEmpire == null || joinEmpire.isAbandoned()) {
            resident.tell(getError("empire-doesnt-exist"));
        } else if (joinEmpire.getAccess().below(LandAccess.OPEN) && !Utils.villageHasInvite(resident.getVillage(), joinEmpire)) {
            resident.tell(getMessage("no-invite"),
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
            resident.addChannel(ChatChannel.EMPIRE);
            InviteCache.getPendingEmpireInvites().remove(resident.getVillage().getId(), joinEmpire.getId());
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(resident.getVillage()));
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
}
