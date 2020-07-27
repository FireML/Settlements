package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.landflags.BuildState;
import me.crz.minecraft.kingdoms.landflags.Use;
import me.crz.minecraft.kingdoms.utils.NameTag;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
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
                    for (Use u : Use.values()) {
                        res.getVillage().setUseState(u, state);
                    }
                    Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
                    res.tell(getMessage("success-all"), "state", state.getTag());
                } catch (IllegalArgumentException e) {
                    res.tell(getMessage("states"), "states", BuildState.values(BuildState.AppliesTo.SETTLEMENT).stream().map(NameTag::humanizedName).collect(Collectors.joining(", ")));
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
            try {
                Use u = Use.valueOf(args[0].toUpperCase());
                BuildState state = BuildState.valueOf(Arrays.stream(args).skip(1).collect(Collectors.joining("_")).toUpperCase(), BuildState.AppliesTo.SETTLEMENT);
                res.getVillage().setUseState(u, state);
                Utils.runAsync(() -> DatabaseManager.getInstance().saveVillage(res.getVillage()));
                res.tell(getMessage("success"), "flag", u.name(), "state", state.getTag());
            } catch (IllegalArgumentException e) {
                res.tell(getMessage("flags"), "flags", Arrays.stream(Use.values()).map(u -> u.name().toLowerCase()).collect(Collectors.joining(", ")));
                res.tell(getMessage("states"), "states", BuildState.values(BuildState.AppliesTo.SETTLEMENT).stream().map(NameTag::humanizedName).collect(Collectors.joining(", ")));
                return false;
            }
        }
        return true;
    }
}
