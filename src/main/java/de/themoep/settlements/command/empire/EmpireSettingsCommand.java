package de.themoep.settlements.command.empire;

import de.themoep.settlements.land.gui.SettingsGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;

public class EmpireSettingsCommand extends EmpireSubCommand {

    public EmpireSettingsCommand(SubCommand parent) {
        super(parent, "settings", "maintenance", "manager", "manage");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        new SettingsGUI(resident, resident.getSettlement().getEmpire()).open();
        return true;
    }
}
