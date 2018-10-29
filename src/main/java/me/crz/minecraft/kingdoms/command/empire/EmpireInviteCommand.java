package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.EmpireSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.InviteCache;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class EmpireInviteCommand extends EmpireSubCommand {
    public EmpireInviteCommand(SubCommand parent) {
        super(parent, "invite <settlement>", EmpireRank.SENATOR);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        Village invitedVillage = VillageManager.getInstance().getVillageFromName(args[0]);
        if (invitedVillage == null) {
            getError("village-not-found").tell(res, "name", args[0]);
            return true;
        }
        if (invitedVillage.hasEmpire()) {
            getMessage("already-in-empire").tell(res, "name", invitedVillage.getEmpire().getName());
            return true;
        }
        InviteCache.getPendingEmpireInvites().put(invitedVillage.getId(), res.getVillage().getEmpire().getId());
        getMessage("received").tell(invitedVillage.getOnlineResidents(), "name", res.getVillage().getEmpire().getName());
        getMessage("joinfee").tell(invitedVillage.getOnlineResidents(), "name", res.getVillage().getEmpire().getName(), "amount", String.valueOf(res.getVillage().getEmpire().getJoinFee()));
        getMessage("alert").tell(res.getVillage().getEmpire().getOnlineResidents(), "name", invitedVillage.getName());
        return true;
    }
}
