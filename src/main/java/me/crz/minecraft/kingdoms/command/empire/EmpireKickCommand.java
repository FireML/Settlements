package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class EmpireKickCommand extends EmpireSubCommand {
    public EmpireKickCommand(SubCommand parent) {
        super(parent, "kick <name>", EmpireRank.SENATOR);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        if (args[0].equalsIgnoreCase(resident.getVillage().getName())) {
             getMessage("cant-kick-yourself").tell(resident);
            return true;
        }
        Utils.runAsync(() -> {
            Village village = VillageManager.getInstance().getVillageFromName(args[0]);
            if (village == null) {
                 getError("village-doesnt-exist").tell(resident);
                return;
            }
            if (!resident.getVillage().getEmpire().equals(village.getEmpire())) {
                 getError("not-in-current").tell(resident, "name", village.getName());
                return;
            }
            if (village.getEmpire().getCapital().equals(village)) {
                 getMessage("cant-kick-capital").tell(resident);
                return;
            }
            MessageUtils.tellResidents(village.getEmpire().getOnlineResidents(), getMessage("kicked-alert"),
                    "name", village.getName(),
                    "state", village.getState().getTag()
            );
            InviteCache.getPendingEmpireInvites().remove(village.getId(), village.getEmpire().getId());
            EmpireManager.getInstance().leaveEmpire(village);
        });
        return true;
    }
}
