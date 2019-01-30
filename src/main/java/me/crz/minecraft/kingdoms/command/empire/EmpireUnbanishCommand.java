package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.BanishCommand;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireUnbanishCommand extends EmpireSubCommand implements BanishCommand {

    public EmpireUnbanishCommand(SubCommand parent) {
        super(parent, "unbanish <player>|s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return unbanish(resident, resident.getVillage().getEmpire(), args);
    }
}
