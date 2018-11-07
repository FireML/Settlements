package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.AllyCommand;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireUnAllyCommand extends EmpireSubCommand implements AllyCommand {
    public EmpireUnAllyCommand(SubCommand parent) {
        super(parent, "unally <name>", EmpireRank.LEADER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return removeAlly(resident, resident.getVillage().getEmpire(), args);
    }
}
