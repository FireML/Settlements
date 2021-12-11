package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.gui.SettingsGUI;
import de.themoep.settlements.command.SubCommand;

public class SettlementSettingsCommand extends SettlementSubCommand {
    public SettlementSettingsCommand(SubCommand parent) {
        super(parent, "settings", "maintenance", "gui");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        new SettingsGUI(resident).open();
        return true;
    }
}
