package de.themoep.settlements.missions;

import java.util.Collection;

public interface MissionTaker<M extends Mission> {

    Collection<MissionData> getMissionData();

    int getExtraMissions();

    void addMissionData(MissionData missionData);

    long getMissionPoints();

    void setMissionPoints(long missionPoints);

    MissionProvider<M> getMissionProvider();
}
