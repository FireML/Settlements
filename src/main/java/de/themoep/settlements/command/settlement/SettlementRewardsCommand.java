package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.gui.RewardsGUI;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.event.EventManager;


public class SettlementRewardsCommand extends SettlementSubCommand {
    public SettlementRewardsCommand(SubCommand parent) {
        super(parent, "rewards");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (EventManager.getInstance().getRewards(res.getSettlement()) == null || EventManager.getInstance().getRewards(res.getSettlement()).isEmpty()) {
            getError("no-rewards").tell(res, "settlement", res.getSettlement().getName(), "settlementstate", res.getSettlement().getStateTag());
        } else {
            new RewardsGUI(res.getSettlement()).open(res.getPlayer());
        }
        return true;
    }
}
