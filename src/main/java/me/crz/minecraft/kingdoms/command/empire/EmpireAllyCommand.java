package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.AllyCommand;
import me.crz.minecraft.kingdoms.resident.Resident;

public class EmpireAllyCommand extends EmpireSubCommand implements AllyCommand {
    public EmpireAllyCommand(SubCommand parent) {
        super(parent, "ally s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return addAlly(resident, resident.getVillage().getEmpire(), args);
    }
}
