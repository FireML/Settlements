package de.themoep.settlements.missions;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;

public interface MissionProvider<M extends Mission> {
    String getName();

    <T extends MissionTaker<M>> boolean distributeNewMissions(T taker);

    String getStateTag();

    MissionTaker<M> getMissionTaker(Resident resident);
}
