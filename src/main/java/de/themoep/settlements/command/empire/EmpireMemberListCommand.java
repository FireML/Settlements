package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Utils;

public class EmpireMemberListCommand extends EmpireSubCommand {
    public EmpireMemberListCommand(SubCommand parent) {
        super(parent, "memberlist", "roster", "members");
    }

    @Override
    public boolean run(Resident sender, String base, String alias, String... args) {
        Utils.runAsync(() -> {
            sender.tell(getMessage("head").getComponent());
            for (Settlement settlement : sender.getSettlement().getEmpire().getMembers()) {
                getMessage("entry").tell(sender, settlement.getReplacements(this));
            }
        });
        return true;
    }
}
