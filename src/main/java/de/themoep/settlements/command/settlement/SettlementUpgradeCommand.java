package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.gui.UpgradeGUI;
import de.themoep.settlements.resident.Resident;

public class SettlementUpgradeCommand extends SettlementSubCommand {
    public SettlementUpgradeCommand(SubCommand parent) {
        super(parent, "upgrade", "deposit", "materials");
        setVisible(false);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        new UpgradeGUI(res, res.getSettlement()).open();
        return true;
    }
}
