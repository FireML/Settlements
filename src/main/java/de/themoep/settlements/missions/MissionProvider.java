package de.themoep.settlements.missions;

import de.themoep.settlements.resident.Resident;

import java.util.Collection;

public interface MissionProvider<M extends Mission> {
    String getName();

    int distributeNewMissions();

    <T extends MissionTaker<M>> int distributeNewMissions(T taker);

    void distributeAdditionalMissions();

    <T extends MissionTaker<M>> int distributeAdditionalMissions(T taker);

    boolean distributeMissionsOnLevelUp();

    String getStateTag();

    MissionTaker<M> getMissionTaker(Resident resident);

    String name();

    long getFinishedMissions();

    void setFinishedMissions(long missions);

    Collection<M> getMissions();

    long getMissionRedistributePrice();
}
