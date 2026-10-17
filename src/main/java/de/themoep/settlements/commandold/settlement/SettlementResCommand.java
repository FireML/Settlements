package de.themoep.settlements.commandold.settlement;

import de.themoep.settlements.commandold.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.gui.ResListGUI;
import de.themoep.settlements.settlement.gui.ResidentGUI;

public class SettlementResCommand extends SettlementSubCommand {

    public SettlementResCommand(SubCommand parent) {
        super(parent, "res", "resident");
    }

    @Override
    public boolean run(Resident sender, String base, String alias, String... args) {
        if (args.length == 0) {
            new ResListGUI(sender, sender.getSettlement()).open();
            return true;
        }
        Resident target = sender.getSettlement().getResident(args[0]);
        if (target != null && sender.getSettlement().isMember(target)) {
            new ResidentGUI(sender, target).open();
        } else {
            getError("not-in-current").tell(sender, "state", sender.getSettlement().getState().getTag());
        }
        return true;
    }
}
