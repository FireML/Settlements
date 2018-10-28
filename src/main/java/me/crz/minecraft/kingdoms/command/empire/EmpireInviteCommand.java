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
            res.tell(getError("village-not-found"), "name", args[0]);
            return true;
        }
        if (invitedVillage.hasEmpire()) {
            res.tell(getMessage("already-in-empire"), "name", invitedVillage.getEmpire().getName());
            return true;
        }
        InviteCache.getPendingEmpireInvites().put(invitedVillage.getId(), res.getVillage().getEmpire().getId());
        MessageUtils.tellResidents(invitedVillage.getOnlineResidents(), getMessage("received"), "name", res.getVillage().getEmpire().getName());
        MessageUtils.tellResidents(res.getVillage().getEmpire().getOnlineResidents(), getMessage("alert"), "player", invitedVillage.getName());
        return true;
    }
}
