package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.BuildState;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.gui.BuildGui;

public class VillageBuildCommand extends VillageSubCommand {
    public VillageBuildCommand(SubCommand parent) {
        super(parent, "build <state>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            new BuildGui().open(res.getPlayer());
            return true;
        }

        try {
            BuildState newState = BuildState.valueOf(args[0].toUpperCase(), BuildState.AppliesTo.SETTLEMENT);
            res.getVillage().setBuildState(newState);
            DatabaseManager.getInstance().saveVillage(res.getVillage());
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
