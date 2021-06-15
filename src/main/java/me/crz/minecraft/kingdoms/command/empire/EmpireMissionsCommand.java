package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.LogType;
import me.crz.minecraft.kingdoms.leveling.DepositGUI;
import me.crz.minecraft.kingdoms.missions.MissionGui;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.utils.Message;

public class EmpireMissionsCommand extends EmpireSubCommand {
    public EmpireMissionsCommand(SubCommand parent) {
        super(parent, "missions", "quests", "mission", "quest");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        new MissionGui(resident).open();
        return true;
    }
}
