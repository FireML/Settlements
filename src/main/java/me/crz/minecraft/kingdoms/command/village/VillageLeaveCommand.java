package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class VillageLeaveCommand extends VillageSubCommand {
    public VillageLeaveCommand(SubCommand parent) {
        super(parent, "leave", VillageRank.RESIDENT);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.getVillageRank() == VillageRank.CHIEF) {
            resident.tell(getMessage("cant-leave-own"), null, null);
        } else {
            resident.tell(getMessage("success"), null, null);
            MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("alert"), "player", resident.getName());
            VillageManager.getInstance().leaveVillage(resident);
        }
        return true;
    }
}
