package de.themoep.settlements.command.empire;

import de.themoep.settlements.leveling.DepositGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.LogType;
import de.themoep.settlements.utils.Message;

public class EmpireDepositCommand extends EmpireSubCommand {
    public EmpireDepositCommand(SubCommand parent) {
        super(parent, "deposit");
        registerCommand(new EmpireHistoryCommand(this, LogType.VAULT));
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getVillage().getEmpire().isMaxLevel()) {
            new DepositGUI("empire", resident, resident.getVillage().getEmpire()).open();
        } else {
            resident.tell(Message.get("command.empire.upgrade.max-level-reached"));
        }
        return true;
    }
}
