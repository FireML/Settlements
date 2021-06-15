package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.missions.Mission;
import me.crz.minecraft.kingdoms.resident.Resident;
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
