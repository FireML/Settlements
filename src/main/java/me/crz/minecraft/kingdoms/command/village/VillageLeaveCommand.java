package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageManager;

public class VillageLeaveCommand extends VillageSubCommand {
    public VillageLeaveCommand(SubCommand parent) {
        super(parent, "leave");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.getVillageRank() == VillageManager.getInstance().getChiefRank()) {
            resident.tell(getMessage("cant-leave-own"));
        } else {
            resident.tell(getMessage("success"));
            MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("alert"), "player", resident.getName());
            resident.leaveVillage();
        }
        return true;
    }
}
