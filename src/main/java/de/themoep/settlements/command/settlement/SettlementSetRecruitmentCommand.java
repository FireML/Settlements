package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.gui.RecruitmentGUI;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.JoinType;

public class SettlementSetRecruitmentCommand extends SettlementSubCommand {
    public SettlementSetRecruitmentCommand(SubCommand parent) {
        super(parent, "recruitment <access>",  "setrecruitment", "setjointype", "jointype");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length < 1) {
            new RecruitmentGUI("settlement", resident.getSettlement()).open(resident.getPlayer());
            return true;
        }
        try {
            JoinType joinType = JoinType.valueOf(String.join("_", args).toUpperCase());
            resident.getSettlement().setJoinType(joinType);
            DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
            resident.tell(getMessage("success"), "joinType", joinType.getTag());
        } catch (IllegalArgumentException e) {
            resident.tell(getMessage("usage.head"),
                    "name", name(),
                    "command", "/" + base + " " + alias,
                    "usage", getUsage(base),
                    "type", resident.getSettlement().getStateTag() + " "
            );
            for (JoinType joinType : JoinType.values()) {
                resident.tell(getMessage("usage.entry"),
                        "usage", "/" + base + " " + alias + " " + joinType.getTag(),
                        "description", getText("access." + joinType.getKey())
                );
            }
            if (getMessage("usage.footer").exists()) {
                resident.tell(getMessage("usage.footer"), "name", name(), "command", "/" + base + " " + alias);
            }
        }
        return true;
    }
}
