package de.themoep.settlements.missions;

import de.themoep.settlements.empire.EmpireMission;
import de.themoep.settlements.settlement.SettlementMission;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MissionData implements ConfigurationSerializable {
    private final Mission mission;
    private final Map<String, Long> progress = new LinkedHashMap<>();

    public MissionData(Mission mission) {
        this.mission = mission;
        for (String type : mission.getRequirements().keySet()) {
            progress.put(type, 0L);
        }
    }

    public Mission getMission() {
        return mission;
    }

    public boolean requires(String type) {
        return mission.getRequirement(type) > getProgress(type);
    }

    public Map<String, Long> getProgress() {
        return progress;
    }

    public long getProgress(String type) {
        return progress.getOrDefault(type.toUpperCase(Locale.ROOT), 0L);
    }

    public void addProgress(String type, long amount) {
        Utils.addAmount(progress, type.toUpperCase(Locale.ROOT), amount);
    }

    public void addProgress(Map<String, Long> map) {
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            addProgress(entry.getKey(), entry.getValue());
        }
    }

    public boolean isFinished() {
        for (Map.Entry<String, Long> entry : mission.getRequirements().entrySet()) {
            if (entry.getValue() > getProgress(entry.getKey())) {
                return false;
            }
        }
        return true;
    }

    public boolean hasProgress() {
        for (Long value : getProgress().values()) {
            if (value > 0) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("mission", mission.getDbId());
        map.put("progress", progress);
        return map;
    }

    public static MissionData deserialize(Map<String, Object> map) {
        if (!(map.get("mission") instanceof String) || !(map.get("progress") instanceof Long)) {
            return null;
        }
        Mission mission = Mission.fromDbId((String) map.get("mission"));
        if (mission == null) {
            return null;
        }
        MissionData missionData = new MissionData(mission);
        missionData.progress.putAll((Map<String, Long>) map.get("progress"));
        return missionData;
    }

    public String[] getReplacements(ConfigMessages messages, MissionProvider<?> provider) {
        return new String[] {
                "title", getMission().getTag(),
                "requirements", getMission().getRequirements().entrySet().stream().map(e ->
                messages.getText("requirement." + (getProgress(e.getKey()) < e.getValue() ? "todo" : "done"),
                        "progress", String.valueOf(getProgress(e.getKey())),
                        "required", String.valueOf(e.getValue()),
                        "type", WordUtils.capitalizeFully(e.getKey().replace('_', ' '))
                )).collect(Collectors.joining("\n")),
                "action", (isFinished() ? messages.getText("finished") : "")
                        + (getMission() instanceof EmpireMission && ((EmpireMission) getMission()).onlyInEmpireWorld()
                                ? messages.getText("only-in-empire", "empire", provider.getName())
                                : getMission() instanceof EmpireMission && ((EmpireMission) getMission()).onlyOnEmpireClaim()
                                        ? messages.getText("only-on-claim", "state", provider.getStateTag(), "name", provider.getName())
                                        : getMission() instanceof SettlementMission && ((SettlementMission) getMission()).onlyOnSettlementClaim()
                                                ? messages.getText("only-on-claim", "state", provider.getStateTag(), "name", provider.getName())
                                                : "")
                        + getMission().getType().getTag(),
                "missionpoints", String.valueOf(getMission().getMissionPoints()),
                "contributionpoints", String.valueOf(getMission().getMissionPoints())
        };
    }
}
