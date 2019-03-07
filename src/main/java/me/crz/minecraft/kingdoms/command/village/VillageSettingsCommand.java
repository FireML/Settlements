package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.gui.SettingsGUI;

public class VillageSettingsCommand extends VillageSubCommand {
    public VillageSettingsCommand(SubCommand parent) {
        super(parent, "settings");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        new SettingsGUI(resident).open();
        return true;
    }
}
