package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.gui.UpgradeGUI;
import de.themoep.settlements.resident.Resident;

public class EmpireUpgradeCommand extends EmpireSubCommand {

    public EmpireUpgradeCommand(SubCommand parent) {
        super(parent, "upgrade", "materials");
        setVisible(false);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        new UpgradeGUI(resident, resident.getSettlement().getEmpire()).open();
        return true;
    }
}
