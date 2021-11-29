package de.themoep.settlements.command.empire;

import de.themoep.settlements.missions.MissionGui;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;

public class EmpireMissionsCommand extends EmpireSubCommand {
    public EmpireMissionsCommand(SubCommand parent) {
        super(parent, "missions", "quests", "mission", "quest");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getSettlement().getMissionData().isEmpty()) {
            new MissionGui(resident).open();
        } else {
            getMessage("wait-for-missions").tell(resident,
                    "state", resident.getSettlement().getStateTag(),
                    "name", resident.getSettlement().getName(),
                    "empire", resident.getSettlement().getEmpire().getName()
            );
        }
        return true;
    }
}
