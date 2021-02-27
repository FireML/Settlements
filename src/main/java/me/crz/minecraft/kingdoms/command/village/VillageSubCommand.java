package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.ResidentPermsSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.resident.Resident;

public abstract class VillageSubCommand extends ResidentPermsSubCommand {

    public VillageSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (!resident.hasVillage()) {
            MessageUtils.tell(resident, getError("village-required"));
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasVillage() && super.hasAccess(resident);
    }

}
