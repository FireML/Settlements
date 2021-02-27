package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.AllyCommand;
import me.crz.minecraft.kingdoms.resident.Resident;

public class EmpireUnAllyCommand extends EmpireSubCommand implements AllyCommand {
    public EmpireUnAllyCommand(SubCommand parent) {
        super(parent, "unally s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return removeAlly(resident, resident.getVillage().getEmpire(), args);
    }
}
