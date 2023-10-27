package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.gui.BuildGUI;
import de.themoep.settlements.resident.Resident;

public class SettlementBuildCommand extends SettlementSubCommand {
    public SettlementBuildCommand(SubCommand parent) {
        super(parent, "build <state>");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            new BuildGUI(
                    "settlement",
                    res.getSettlement(),
                    () -> res.hasSettlement() && res.hasPermission(getPermission())
            ).open(res.getPlayer());
            return true;
        }

        try {
            BuildState newState = BuildState.valueOf(args[0].toUpperCase(), BuildState.AppliesTo.SETTLEMENT);
            res.getSettlement().setBuildState(newState);
            DatabaseManager.getInstance().saveSettlement(res.getSettlement());
            res.tell(getMessage("success").getText(), "state", newState.getTag(), "type", res.getSettlement().getBuildTag());
        } catch (IllegalArgumentException e) {
            res.tell(getMessage("usage.head").getText(), "name", name(), "command", "/" + base + " " + alias, "usage", getUsage(base));
            for (BuildState state : BuildState.values(BuildState.AppliesTo.SETTLEMENT)) {
                res.tell(getMessage("usage.entry").getText(),
                        "usage", getUsage(base).replace("<state>", state.name().toLowerCase()),
                        "description", getMessage("states." + state.getKey()).getText()
                );
            }
        }
        return true;
    }
}
