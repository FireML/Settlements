package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.settlement.SettlementDepositCommand;
import de.themoep.settlements.land.gui.DepositGUI;
import de.themoep.settlements.resident.Resident;

public class EmpireDepositCommand extends EmpireSubCommand {
    public EmpireDepositCommand(SubCommand parent) {
        super(parent, "upgrade", "materials", "deposit");
        setVisible(false);
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        new DepositGUI<>(res.getSettlement().getEmpire().getKey(), res, SettlementDepositCommand.getLevellable(res.getSettlement().getEmpire())).open();
        return true;
    }
}
