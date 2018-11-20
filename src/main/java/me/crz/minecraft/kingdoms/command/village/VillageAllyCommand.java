package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.common.AllyCommand;
import me.crz.minecraft.kingdoms.village.Resident;

import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageAllyCommand extends VillageSubCommand implements AllyCommand {
    public VillageAllyCommand(SubCommand parent) {
        super(parent, "ally s:<settlement>|e:<empire>", VillageRank.ELDER);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return addAlly(resident, resident.getVillage(), args);
    }
}
