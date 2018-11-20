package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireLeaveCommand extends EmpireSubCommand {
    public EmpireLeaveCommand(SubCommand parent) {
        super(parent, "leave", EmpireRank.MEMBER, VillageRank.ELDER);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.getVillage().equals(resident.getVillage().getEmpire().getCapital())) {
            getMessage("cant-leave-own").tell(resident);
        } else {
            getMessage("success").tell(resident.getVillage().getOnlineResidents(),
                    "state", resident.getVillage().getState().getTag(),
                    "empire", resident.getVillage().getEmpire().getName()
            );
            MessageUtils.tellResidents(resident.getVillage().getEmpire().getOnlineResidents(), getMessage("alert"),
                    "name", resident.getVillage().getName(),
                    "state", resident.getVillage().getState().getTag()
            );
            EmpireManager.getInstance().leaveEmpire(resident.getVillage());
        }
        return true;
    }
}
