package me.crz.minecraft.kingdoms.scoreboard.commands;

import me.crz.minecraft.kingdoms.command.ResidentSubCommand;
import me.crz.minecraft.kingdoms.resident.Resident;


public class CommandScoreboard extends ResidentSubCommand {

    public CommandScoreboard() {
        super(null, "scoreboard", "sb", "board", "sidebar", "side");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (resident.showSidebar()) {
            resident.showSidebar(false);
            getMessage("disabled").tell(resident);
        } else {
            resident.showSidebar(true);
            getMessage("enabled").tell(resident);
        }
        return true;
    }

}
