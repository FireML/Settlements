package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.gui.BuildGUI;

public class EmpireBuildCommand extends EmpireSubCommand {
    public EmpireBuildCommand(SubCommand parent) {
        super(parent, "build <state>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            new BuildGUI(
                    "empire",
                    res.getVillage().getEmpire(),
                    () -> res.hasEmpire() && res.hasPermission(getPermission())
            ).open(res.getPlayer());
            return true;
        }

        try {
            BuildState newState = BuildState.valueOf(args[0].toUpperCase(), BuildState.AppliesTo.EMPIRE);
            res.getVillage().getEmpire().setBuildState(newState);
            DatabaseManager.getInstance().saveEmpire(res.getVillage().getEmpire());
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
