package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.gui.RewardsGUI;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.event.EventManager;


public class VillageRewardsCommand extends VillageSubCommand {
    public VillageRewardsCommand(SubCommand parent) {
        super(parent, "rewards");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (EventManager.getInstance().getRewards(res.getVillage()) == null || EventManager.getInstance().getRewards(res.getVillage()).isEmpty()) {
            getError("no-rewards").tell(res, "village", res.getVillage().getName(), "villagestate", res.getVillage().getStateTag());
        } else {
            new RewardsGUI(res.getVillage()).open(res.getPlayer());
        }
        return true;
    }
}
