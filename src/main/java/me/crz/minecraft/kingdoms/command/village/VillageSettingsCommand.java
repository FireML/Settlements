package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.village.gui.SettingsGUI;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.command.SubCommand;

public class VillageSettingsCommand extends VillageSubCommand {
    public VillageSettingsCommand(SubCommand parent) {
        super(parent, "settings", VillageRank.CHIEF);
        setPermission("kingdoms.user");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        SettingsGUI.getInstance().open(resident.getPlayer());
        return true;
    }
}
