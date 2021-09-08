package de.themoep.settlements.command.empire;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.empire.gui.UseGUI;
import de.themoep.settlements.land.BuildState;
import de.themoep.settlements.land.UseFlag;
import de.themoep.settlements.land.UseFlagManager;
import de.themoep.settlements.utils.NameTag;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EmpireUseCommand extends EmpireSubCommand {
    public EmpireUseCommand(SubCommand parent) {
        super(parent, "use <name> <state>");
        registerCommand(new EmpireSubCommand(this, "all <state>") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                try {
                    BuildState state = BuildState.valueOf(String.join("_", args).toUpperCase(), BuildState.AppliesTo.EMPIRE);
                    for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
                        res.getVillage().getEmpire().setUseState(u, state);
                    }
                    DatabaseManager.getInstance().saveEmpire(res.getVillage().getEmpire());
                    getMessage("success-all").tell(res, "state", state.getTag());
                } catch (IllegalArgumentException e) {
                    getMessage("states").tell(res, "states", BuildState.values(BuildState.AppliesTo.EMPIRE).stream().map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 2) {
            new UseGUI().open(res.getPlayer());
        } else {
            UseFlag useFlag = UseFlagManager.getInstance().get(args[0]);
            if (useFlag != null) {
                BuildState state = BuildState.valueOf(Arrays.stream(args).skip(1).collect(Collectors.joining("_")).toUpperCase(), BuildState.AppliesTo.EMPIRE);
                res.getVillage().getEmpire().setUseState(useFlag, state);
                DatabaseManager.getInstance().saveEmpire(res.getVillage().getEmpire());
                getMessage("success").tell(res, "flag", useFlag.name(), "state", state.getTag());
            } else {
                getMessage("flags").tell(res, "flags", UseFlagManager.getInstance().getUseFlags().stream().map(u -> u.name().toLowerCase()).collect(Collectors.joining(", ")));
                getMessage("states").tell(res, "states", BuildState.values(BuildState.AppliesTo.EMPIRE).stream().map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                return false;
            }
        }
        return true;
    }
}
