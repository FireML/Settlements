package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.village.gui.BuildGui;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.landflags.BuildState;

import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageBuildCommand extends VillageSubCommand {
    public VillageBuildCommand(SubCommand parent) {
        super(parent, "build <state>", VillageRank.CHIEF);
        this.setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            BuildGui.getInstance().open(res.getPlayer());
            return true;
        }

        try {
            BuildState newState = BuildState.valueOf(args[0].toUpperCase(), BuildState.AppliesTo.SETTLEMENT);
            res.getVillage().setBuildState(newState);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
            res.tell(getMessage("success"), "state", newState.getTag());
        } catch (IllegalArgumentException e) {
            res.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base));
            for (BuildState state : BuildState.values(BuildState.AppliesTo.SETTLEMENT)) {
                res.tell(getMessage("usage.entry"),
                        "usage", getUsage(base).replace("<state>", state.name().toLowerCase()),
                        "description", getMessage("states." + state.getKey()).getText()
                );
            }
        }
        return true;
    }
}
