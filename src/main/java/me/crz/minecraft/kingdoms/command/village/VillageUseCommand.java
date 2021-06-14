package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.BuildState;
import me.crz.minecraft.kingdoms.land.UseFlag;
import me.crz.minecraft.kingdoms.land.UseFlagManager;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.gui.UseGui;

import java.util.Arrays;
import java.util.stream.Collectors;

public class VillageUseCommand extends VillageSubCommand {
    public VillageUseCommand(SubCommand parent) {
        super(parent, "use <name> <state>");
        registerCommand(new VillageSubCommand(this, "all <state>") {
            @Override
            public boolean run(Resident res, String base, String alias, String... args) {
                try {
                    BuildState state = BuildState.valueOf(String.join("_", args).toUpperCase(), BuildState.AppliesTo.SETTLEMENT);
                    for (UseFlag u : UseFlagManager.getInstance().getUseFlags()) {
                        res.getVillage().setUseState(u, state);
                    }
                    DatabaseManager.getInstance().saveVillage(res.getVillage());
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
            new UseGui().open(res.getPlayer());
        } else {
            UseFlag useFlag = UseFlagManager.getInstance().get(args[0]);
            if (useFlag != null) {
                BuildState state = BuildState.valueOf(Arrays.stream(args).skip(1).collect(Collectors.joining("_")).toUpperCase(), BuildState.AppliesTo.SETTLEMENT);
                res.getVillage().setUseState(useFlag, state);
                DatabaseManager.getInstance().saveVillage(res.getVillage());
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
