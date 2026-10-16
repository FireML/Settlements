package de.themoep.settlements.empire;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.missions.Mission;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.resident.Resident;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class EmpireMission extends Mission {
    private final boolean inEmpireWorld;
    private final boolean onEmpireClaim;

    public EmpireMission(ConfigurationSection config) {
        super(config);
        inEmpireWorld = config.getBoolean("in-empire-world", true);
        onEmpireClaim = config.getBoolean("on-empire-claim", false);
    }

    @Override
    protected String getDbName() {
        return "empiremission";
    }

    @Override
    public void finish(Resident resident) {
        EmpireManager.getInstance().finishMission(this, resident);
    }

    @Override
    public boolean isValid(Resident resident, MissionProvider<?> provider) {
        if (provider instanceof Empire) {
            Player player = resident.getPlayer();
            if (player == null) {
                return false;
            }
            if (onlyInEmpireWorld()) {
                return ((Empire) provider).hasWorld()
                        && player.getWorld().getName().equalsIgnoreCase(((Empire) provider).getWorldName());
            }
            if (onlyOnEmpireClaim()) {
                OwnedLand land = LandManager.getInstance().getLand(player.getLocation());
                if (land != null && land.isOwnedBy((Empire) provider)) {
                    if (!onlyInEmpireWorld()) {
                        return ((Empire) provider).hasWorld()
                                || !player.getWorld().getName().equalsIgnoreCase(((Empire) provider).getWorldName());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onlyInEmpireWorld() {
        return inEmpireWorld;
    }

    public boolean onlyOnEmpireClaim() {
        return onEmpireClaim;
    }
}
