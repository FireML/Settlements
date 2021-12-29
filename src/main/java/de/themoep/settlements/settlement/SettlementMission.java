package de.themoep.settlements.settlement;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.missions.Mission;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.resident.Resident;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SettlementMission extends Mission {
    private final boolean onSettlementClaim;

    public SettlementMission(ConfigurationSection config) {
        super(config);
        onSettlementClaim = config.getBoolean("on-settlement-claim", true);
    }

    @Override
    protected String getDbName() {
        return "settlementmission";
    }

    @Override
    public void finish(Resident resident) {
        SettlementManager.getInstance().finishMission(this, resident);
    }

    @Override
    public boolean isValid(Resident resident, MissionProvider provider) {
        if (provider instanceof Settlement) {
            if (!onlyOnSettlementClaim()) {
                return true;
            }
            Player player = resident.getPlayer();
            if (player == null) {
                return false;
            }
            OwnedLand land = LandManager.getInstance().getLand(player.getLocation());
            return land != null && land.isOwnedBy((Settlement) provider);
        }
        return false;
    }

    public boolean onlyOnSettlementClaim() {
        return onSettlementClaim;
    }
}
