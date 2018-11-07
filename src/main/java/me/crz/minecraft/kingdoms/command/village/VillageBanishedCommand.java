package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.common.BanishCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

public class VillageBanishedCommand extends VillageSubCommand implements BanishCommand {
    public VillageBanishedCommand(SubCommand parent) {
        super(parent, "banished", VillageRank.CHIEF, "banishlist", "listbanished");
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return listBanished(resident, resident.getVillage());
    }

}
