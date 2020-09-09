package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.empire.gui.SettingsGUI;
import me.crz.minecraft.kingdoms.village.Resident;

public class EmpireSettingsCommand extends EmpireSubCommand {
    public EmpireSettingsCommand(SubCommand parent) {
        super(parent, "settings", "gui");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        new SettingsGUI(resident).open();
        return true;
    }
}
