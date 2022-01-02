package de.themoep.settlements.command.empire;

import de.themoep.settlements.command.common.MissionCommand;
import de.themoep.settlements.missions.MissionGui;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.utils.config.Config;

public class EmpireMissionsCommand extends EmpireSubCommand implements MissionCommand {
    public EmpireMissionsCommand(SubCommand parent) {
        super(parent, "missions", "quests", "mission", "quest");
        if (Config.Empire.Missions.REDISTRIBUTE_PRICE > -1) {
            registerCommand(new EmpireSubCommand(this, "redistribute", "reset", "distribute") {
                @Override
                public boolean run(Resident resident, String base, String alias, String... args) {
                    distributeNewMissions(resident, resident.getSettlement().getEmpire());
                    return true;
                }
            });
        }
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getSettlement().getMissionData().isEmpty()) {
            new MissionGui(resident, resident.getSettlement().getEmpire(), resident.getSettlement()).open();
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
