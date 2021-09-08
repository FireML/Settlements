package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;

public class VillageJoinFeeCommand extends VillageSubCommand {
    public VillageJoinFeeCommand(SubCommand parent) {
        super(parent, "joinfee [set <amount>]");
        registerCommand(new VillageSubCommand(this, "set <amount>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                if (args.length == 0) {
                    return false;
                }
                try {
                    long amount = Long.parseLong(args[0]);
                    resident.getVillage().setJoinFee(amount);
                    getMessage("success").tell(resident.getVillage().getOnlineResidents(),
                            "state", resident.getVillage().getStateTag(),
                            "amount", String.valueOf(resident.getVillage().getJoinFee())
                    );
                    DatabaseManager.getInstance().saveSettlement(resident.getVillage());
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
                    "state", resident.getVillage().getStateTag(),
                    "amount", String.valueOf(resident.getVillage().getJoinFee())
            );
            return true;
        }
        return false;
    }
}
