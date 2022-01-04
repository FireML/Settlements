package de.themoep.settlements.command.settlement;

import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.command.common.MissionCommand;
import de.themoep.settlements.missions.MissionGui;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.SettlementMission;
import de.themoep.settlements.utils.config.Config;

public class SettlementMissionsCommand extends SettlementSubCommand implements MissionCommand<SettlementMission> {
    public SettlementMissionsCommand(SubCommand parent) {
        super(parent, "missions", "quests", "mission", "quest");
        if (Config.Settlement.Missions.REDISTRIBUTE_PRICE > -1) {
            registerCommand(new SettlementSubCommand(this, "redistribute", "reset", "distribute") {
                @Override
                public boolean run(Resident resident, String base, String alias, String... args) {
                    distributeNewMissions(resident, resident.getSettlement());
                    return true;
                }
            });
        }
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
