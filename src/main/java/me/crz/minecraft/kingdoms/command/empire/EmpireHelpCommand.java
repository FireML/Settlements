package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.HelpCommand;
import me.crz.minecraft.kingdoms.rank.Rank;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireHelpCommand extends EmpireSubCommand {
    public EmpireHelpCommand(HelpCommand parent, Rank rank) {
        super(parent, rank.name().toLowerCase() + " [<page>]");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        return getParent().run(resident.getPlayer(),
                base.contains(" ") ? base.substring(0, base.lastIndexOf(' ')) : "",
                base.contains(" ") ? base.substring(base.lastIndexOf(' ')) : base, (name() + " " + String.join(" ", args)).split(" "));
    }
}
