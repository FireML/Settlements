package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.AllyCommand;
import me.crz.minecraft.kingdoms.resident.Resident;

public class VillageAlliesCommand extends VillageSubCommand implements AllyCommand {
    public VillageAlliesCommand(SubCommand parent) {
        super(parent, "allies");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return listAllies(resident, resident.getVillage());
    }
}
