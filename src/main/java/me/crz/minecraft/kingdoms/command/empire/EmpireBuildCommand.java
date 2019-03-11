package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.empire.gui.BuildGui;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireBuildCommand extends EmpireSubCommand {
    public EmpireBuildCommand(SubCommand parent) {
        super(parent, "build <state>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            BuildGui.getInstance().open(res.getPlayer());
            return true;
        }

        try {
            BuildState newState = BuildState.valueOf(args[0].toUpperCase(), BuildState.AppliesTo.EMPIRE);
            res.getVillage().getEmpire().setBuildState(newState);
            Utils.runAsync(() -> DatabaseManager.getInstance().saveEmpire(res.getVillage().getEmpire()));
            res.tell(getMessage("success"), "state", newState.getTag());
        } catch (IllegalArgumentException e) {
            res.tell(getMessage("usage.head"), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base));
            for (BuildState state : BuildState.values(BuildState.AppliesTo.EMPIRE)) {
                res.tell(getMessage("usage.entry"),
                        "usage", getUsage(base).replace("<state>", state.name().toLowerCase()),
                        "description", getMessage("states." + state.getKey()).getText()
                );
            }
        }
        return true;
    }
}
