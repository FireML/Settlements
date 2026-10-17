package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;

import de.themoep.settlements.land.gui.DepositGUI;
import de.themoep.settlements.resident.Resident;

public class SettlementDepositCommand extends SettlementSubCommand {

    public SettlementDepositCommand(SubCommand parent) {
        super(parent, "deposit");
        setVisible(false);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        new DepositGUI(res.getSettlement().getKey(), res, res.getSettlement()).open();
        return true;
    }

}
