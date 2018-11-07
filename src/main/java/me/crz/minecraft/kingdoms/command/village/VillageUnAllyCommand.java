package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.AllyCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

public class VillageUnAllyCommand extends VillageSubCommand implements AllyCommand {
    public VillageUnAllyCommand(SubCommand parent) {
        super(parent, "unally <name>", VillageRank.ELDER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }
        return removeAlly(resident, resident.getVillage(), args);
    }
}
