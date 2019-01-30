package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;

public abstract class EmpireSubCommand extends VillageSubCommand {

    public EmpireSubCommand(SubCommand parent, String usage, String... aliases) {
        super(parent, usage, aliases);
    }

    @Override
    public boolean testAccess(Resident resident) {
        if (!resident.hasEmpire()) {
            MessageUtils.tell(resident, getError("empire-required"));
            return false;
        }
        return super.testAccess(resident);
    }

    @Override
    public boolean hasAccess(Resident resident) {
        return resident.hasEmpire() && super.hasAccess(resident);
    }
}
