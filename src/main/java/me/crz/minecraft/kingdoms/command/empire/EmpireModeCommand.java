package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.gui.ModeGUI;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.village.Resident;

import java.util.Arrays;
import java.util.stream.Collectors;

public class EmpireModeCommand extends EmpireSubCommand {
    public EmpireModeCommand(SubCommand parent) {
        super(parent, "mode <mode> <value>", EmpireRank.SENATOR);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        if (args.length < 1) {
            ModeGUI.getInstance().open(res.getPlayer());
            return true;
        }

        try {
            EmpireMode m = EmpireMode.valueOf(args[0].toLowerCase());
            if (m.getOptions().containsKey(args[1].toLowerCase())) {
                m.set(m.getOptions().get(args[1].toLowerCase()), res.getVillage().getEmpire());
                res.tell(getMessage("success"), "mode", m.name(), "value", args[1]);
            } else {
                return false;
            }
        } catch (IllegalArgumentException e) {
            res.tell(getMessage("modes"), "modes", Arrays.stream(EmpireMode.values()).map(Enum::name).collect(Collectors.joining(", ")));
            return false;
        }
        return true;
    }
}
