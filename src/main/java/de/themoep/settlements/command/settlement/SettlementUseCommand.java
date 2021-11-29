package de.themoep.settlements.command.settlement;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.land.gui.PermsGUI;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.utils.NameTag;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SettlementUseCommand extends SettlementSubCommand {
    public SettlementUseCommand(SubCommand parent) {
        super(parent, "perms [<name> <state>]", "permissions", "rights", "use", "build");
        registerCommand(new SettlementSubCommand(this, "all <state>") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                try {
                    BuildState state = BuildState.valueOf(String.join("_", args).toUpperCase(), BuildState.AppliesTo.SETTLEMENT);
                    for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
                        res.getSettlement().setUseState(u, state);
                    }
                    DatabaseManager.getInstance().saveSettlement(res.getSettlement());
                    getMessage("success-all").tell(res, "state", state.getTag());
                } catch (IllegalArgumentException e) {
                    getMessage("states").tell(res, "states", BuildState.values(BuildState.AppliesTo.SETTLEMENT).stream().map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 2) {
            new PermsGUI(
                    "settlement",
                    res.getSettlement(),
                    () -> res.hasSettlement() && res.hasPermission(getPermission())
            ).open(res.getPlayer());
        } else {
            UseFlag useFlag = UseFlagManager.getInstance().get(args[0]);
            if (useFlag != null) {
                BuildState state = BuildState.valueOf(Arrays.stream(args).skip(1).collect(Collectors.joining("_")).toUpperCase(), BuildState.AppliesTo.SETTLEMENT);
                res.getSettlement().setUseState(useFlag, state);
                DatabaseManager.getInstance().saveSettlement(res.getSettlement());
                getMessage("success").tell(res, "flag", useFlag.name(), "state", state.getTag());
            } else {
                getMessage("flags").tell(res, "flags", UseFlagManager.getInstance().getUseFlags().stream().map(u -> u.name().toLowerCase()).collect(Collectors.joining(", ")));
                getMessage("states").tell(res, "states", BuildState.values(BuildState.AppliesTo.SETTLEMENT).stream().map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                return false;
            }
        }
        return true;
    }
}
