package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Arrays;
import java.util.stream.Collectors;

public class VillageModeCommand extends VillageSubCommand {
    public VillageModeCommand(SubCommand parent) {
        super(parent, "mode <mode> <value>", VillageRank.ELDER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            return false;
        }

        try {
            Mode m = Mode.valueOf(args[0].toLowerCase());
            if (m.getOptions().containsKey(args[1].toLowerCase())) {
                m.set(m.getOptions().get(args[1].toLowerCase()), res.getVillage());
                res.tell(getMessage("success"), "mode", m.name(), "value", args[1]);
            } else {
                return false;
            }
        } catch (IllegalArgumentException e) {
            res.tell(getMessage("modes"), "modes", Arrays.stream(Mode.values()).map(Enum::name).collect(Collectors.joining(", ")));
            return false;
        }
        return true;
    }
}
