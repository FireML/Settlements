package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostClaimCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostListCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostSethomeCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostTeleportCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostUnclaimCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

public class VillageOutPostCommand extends VillageSubCommand {

    public VillageOutPostCommand(SubCommand parent) {
        super(parent, "outpost <options>", VillageRank.RESIDENT);
        setPermission("kingdoms.user");
        getHelpCommand().registerCommand(new VillageHelpCommand(getHelpCommand(), VillageRank.RECRUITER));
        getHelpCommand().registerCommand(new VillageHelpCommand(getHelpCommand(), VillageRank.BUILDER));
        getHelpCommand().registerCommand(new VillageHelpCommand(getHelpCommand(), VillageRank.ELDER));
        getHelpCommand().registerCommand(new VillageHelpCommand(getHelpCommand(), VillageRank.CHIEF));
        registerCommand(new OutpostClaimCommand(this));
        registerCommand(new OutpostUnclaimCommand(this));
        registerCommand(new OutpostSethomeCommand(this));
        registerCommand(new OutpostTeleportCommand(this));
        registerCommand(new OutpostListCommand(this));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return false;
    }
}
