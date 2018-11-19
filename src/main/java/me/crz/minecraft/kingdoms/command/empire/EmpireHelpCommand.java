package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireHelpCommand extends EmpireSubCommand {
    public EmpireHelpCommand(HelpCommand parent, VillageRank rank) {
        super(parent, rank.name().toLowerCase() + " [<page>]");
    }

    public EmpireHelpCommand(HelpCommand parent, EmpireRank rank) {
        super(parent, rank.name().toLowerCase() + " [<page>]");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return getParent().run(resident.getPlayer(),
                base.contains(" ") ? base.substring(0, base.lastIndexOf(' ')) : "",
                base.contains(" ") ? base.substring(base.lastIndexOf(' ')) : base, (name() + " " + String.join(" ", args)).split(" "));
    }
}
