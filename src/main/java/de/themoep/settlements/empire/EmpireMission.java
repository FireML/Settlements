package de.themoep.settlements.empire;

import de.themoep.settlements.missions.Mission;
import de.themoep.settlements.resident.Resident;
import org.bukkit.configuration.ConfigurationSection;

public class EmpireMission extends Mission {
    private final boolean inEmpireWorld;

    public EmpireMission(ConfigurationSection config) {
        super(config);
        inEmpireWorld = config.getBoolean("in-empire-world", true);
    }

    @Override
    protected String getDbName() {
        return "empiremission";
    }

    @Override
    public void finish(Resident resident) {
        EmpireManager.getInstance().finishMission(this, resident);
    }

    public boolean onlyInEmpireWorld() {
        return inEmpireWorld;
    }
}
