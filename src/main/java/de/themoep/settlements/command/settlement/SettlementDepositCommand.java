package de.themoep.settlements.command.settlement;

import de.themoep.settlements.leveling.DepositGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.LogType;
import de.themoep.settlements.utils.Message;

public class SettlementDepositCommand extends SettlementSubCommand {
    public SettlementDepositCommand(SubCommand parent) {
        super(parent, "deposit");
        registerCommand(new SettlementHistoryCommand(this, LogType.VAULT));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getSettlement().isMaxLevel()) {
            new DepositGUI("settlement", resident, resident.getSettlement()).open();
        } else {
            resident.tell(Message.get("command.settlement.upgrade.max-level-reached"));
        }
        return true;
    }
}
