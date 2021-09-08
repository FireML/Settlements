package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.gui.SettingsGUI;
import de.themoep.settlements.command.SubCommand;

public class VillageSettingsCommand extends VillageSubCommand {
    public VillageSettingsCommand(SubCommand parent) {
        super(parent, "settings", "gui");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        new SettingsGUI(resident).open();
        return true;
    }
}
