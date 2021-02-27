package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.BanishCommand;
import me.crz.minecraft.kingdoms.resident.Resident;

public class VillageBanishCommand extends VillageSubCommand implements BanishCommand {

    public VillageBanishCommand(SubCommand parent) {
        super(parent, "banish <player>|s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return banish(resident, resident.getVillage(), args);
    }
}
