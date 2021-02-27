package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.BanishCommand;
import me.crz.minecraft.kingdoms.resident.Resident;

public class VillageUnbanishCommand extends VillageSubCommand implements BanishCommand {

    public VillageUnbanishCommand(SubCommand parent) {
        super(parent, "unbanish <player>|s:<settlement>|e:<empire>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return unbanish(resident, resident.getVillage(), args);
    }
}
