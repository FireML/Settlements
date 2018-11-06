package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.gui.SettingsGUI;
import me.crz.minecraft.kingdoms.rank.EmpireRank;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireSettingsCommand extends EmpireSubCommand {
    public EmpireSettingsCommand(SubCommand parent) {
        super(parent, "settings", EmpireRank.LEADER);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        SettingsGUI.getInstance().open(resident.getPlayer());
        return true;
    }
}
