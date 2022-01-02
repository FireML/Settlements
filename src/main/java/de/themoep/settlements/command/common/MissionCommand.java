package de.themoep.settlements.command.common;

import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;

public interface MissionCommand extends ConfigMessages {

    default void distributeNewMissions(Resident resident, MissionProvider<?> missionProvider) {
        if (missionProvider.getMissionRedistributePrice() > 0) {
            if (missionProvider instanceof LandOwner) {
                if (!((LandOwner) missionProvider).removeFromBank(missionProvider.getMissionRedistributePrice())) {
                    getError("not-enough-money").tell(resident,
                            "amount", Utils.CURRENCY_FORMAT.format(missionProvider.getMissionRedistributePrice())
                    );
                    return;
                }
                DatabaseManager.getInstance().log((LandOwner) missionProvider, resident, "new-missions", "cash", missionProvider.getMissionRedistributePrice());
            }
        }
        missionProvider.distributeNewMissions();
        getMessage("success").tell(resident,
                "amount", String.valueOf(missionProvider.getMissionTaker(resident).getMissionData().size()),
                "price", Utils.CURRENCY_FORMAT.format(missionProvider.getMissionRedistributePrice())
        );
    }
}
