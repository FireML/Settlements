package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.event.EventManager;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.gui.RewardsGUI;


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
