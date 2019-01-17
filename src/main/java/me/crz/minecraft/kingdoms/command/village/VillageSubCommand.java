package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;

public abstract class VillageSubCommand extends ResidentSubCommand {

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
