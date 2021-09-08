package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.Utils;

public class EmpireMemberListCommand extends EmpireSubCommand {
    public EmpireMemberListCommand(SubCommand parent) {
        super(parent, "memberlist", "roster", "members");
    }

    @Override
    public boolean run(Resident sender, String base, String alias, String... args) {
        Utils.runAsync(() -> {
            sender.tell(getMessage("head"));
            for (Village village : sender.getVillage().getEmpire().getMembers()) {
                getMessage("entry").tell(sender, village.getReplacements(this));
            }
        });
        return true;
    }
}
