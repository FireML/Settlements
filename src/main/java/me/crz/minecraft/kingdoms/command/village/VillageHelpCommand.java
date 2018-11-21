package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

public class VillageHelpCommand extends VillageSubCommand {
    public VillageHelpCommand(HelpCommand parent, VillageRank rank) {
        super(parent, rank != null ? rank.name().toLowerCase() : "no" + " [<page>]", rank);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return getParent().run(resident.getPlayer(),
                base.contains(" ") ? base.substring(0, base.lastIndexOf(' ')) : "",
                base.contains(" ") ? base.substring(base.lastIndexOf(' ')) : base, (name() + " " + String.join(" ", args)).split(" "));
    }
}
