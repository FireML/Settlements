package me.crz.minecraft.kingdoms.command.empire;

import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.missions.MissionGui;
import me.crz.minecraft.kingdoms.resident.Resident;

public class EmpireMissionsCommand extends EmpireSubCommand {
    public EmpireMissionsCommand(SubCommand parent) {
        super(parent, "missions", "quests", "mission", "quest");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getVillage().getMissionData().isEmpty()) {
            new MissionGui(resident).open();
        } else {
            getMessage("wait-for-missions").tell(resident,
                    "state", resident.getVillage().getStateTag(),
                    "name", resident.getVillage().getName(),
                    "empire", resident.getVillage().getEmpire().getName()
            );
        }
        return true;
    }
}
