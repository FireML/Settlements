package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.BanishCommand;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireBanishCommand extends EmpireSubCommand implements BanishCommand {

    public EmpireBanishCommand(SubCommand parent) {
        super(parent, "banish <player>|s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return banish(resident, resident.getVillage().getEmpire(), args);
    }
}
