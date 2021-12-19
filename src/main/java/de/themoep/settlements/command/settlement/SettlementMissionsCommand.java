package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.empire.EmpireSubCommand;
import de.themoep.settlements.missions.MissionGui;
import de.themoep.settlements.resident.Resident;

public class SettlementMissionsCommand extends SettlementSubCommand {
    public SettlementMissionsCommand(SubCommand parent) {
        super(parent, "missions", "quests", "mission", "quest");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getMissionData().isEmpty()) {
            new MissionGui(resident, resident.getSettlement(), resident).open();
        } else {
            getMessage("wait-for-missions").tell(resident,
                    "state", resident.getSettlement().getStateTag(),
                    "name", resident.getSettlement().getName()
            );
        }
        return true;
    }
}
