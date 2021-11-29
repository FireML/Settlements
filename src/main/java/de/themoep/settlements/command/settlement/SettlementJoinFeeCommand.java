package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;

public class SettlementJoinFeeCommand extends SettlementSubCommand {
    public SettlementJoinFeeCommand(SubCommand parent) {
        super(parent, "joinfee [set <amount>]");
        registerCommand(new SettlementSubCommand(this, "set <amount>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    return false;
                }
                try {
                    long amount = Long.parseLong(args[0]);
                    resident.getSettlement().setJoinFee(amount);
                    getMessage("success").tell(resident.getSettlement().getOnlineResidents(),
                            "state", resident.getSettlement().getStateTag(),
                            "amount", String.valueOf(resident.getSettlement().getJoinFee())
                    );
                    DatabaseManager.getInstance().saveSettlement(resident.getSettlement());
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(resident, "input", args[0]);
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 0) {
            getMessage("current").tell(resident,
                    "state", resident.getSettlement().getStateTag(),
                    "amount", String.valueOf(resident.getSettlement().getJoinFee())
            );
            return true;
        }
        return false;
    }
}
