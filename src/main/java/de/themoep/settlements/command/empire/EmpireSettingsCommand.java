package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.empire.gui.SettingsGUI;

public class EmpireSettingsCommand extends EmpireSubCommand {
    public EmpireSettingsCommand(SubCommand parent) {
        super(parent, "settings", "gui");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        new SettingsGUI(resident).open();
        return true;
    }
}
