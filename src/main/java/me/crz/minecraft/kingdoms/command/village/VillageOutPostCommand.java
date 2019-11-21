package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostClaimCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostListCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostRenameCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostSethomeCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostTeleportCommand;
import me.crz.minecraft.kingdoms.command.village.outpost.OutpostUnclaimCommand;
import me.crz.minecraft.kingdoms.village.Resident;

public class VillageOutPostCommand extends VillageSubCommand {

    public VillageOutPostCommand(SubCommand parent) {
        super(parent, "outpost <options>");
        setHelpCommand(new HelpCommand(this));
        registerCommand(new OutpostClaimCommand(this));
        registerCommand(new OutpostUnclaimCommand(this));
        registerCommand(new OutpostRenameCommand(this));
        registerCommand(new OutpostSethomeCommand(this));
        registerCommand(new OutpostTeleportCommand(this));
        registerCommand(new OutpostListCommand(this));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return false;
    }
}
