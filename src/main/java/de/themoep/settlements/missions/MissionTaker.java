package de.themoep.settlements.missions;

import java.util.Collection;

public interface MissionTaker<M extends Mission> {

    MissionData getMissionData(String missionId);

    Collection<MissionData> getMissionData();

    int getExtraMissions();

    void addMissionData(MissionData missionData);

    long getMissionPoints();

    void setMissionPoints(long missionPoints);

    MissionProvider<M> getMissionProvider();

    default boolean hasMission(M mission) {
        return getMissionData(mission.getId()) != null;
    }

    default int distributeNewMissions() {
        return getMissionProvider().distributeNewMissions();
    }

}
