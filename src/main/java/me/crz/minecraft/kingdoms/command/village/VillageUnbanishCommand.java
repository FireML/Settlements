package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.BanishCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

public class VillageUnbanishCommand extends VillageSubCommand implements BanishCommand {

    public VillageUnbanishCommand(SubCommand parent) {
        super(parent, "unbanish <name>", VillageRank.CHIEF);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return unbanish(resident, resident.getVillage(), args);
    }
}
