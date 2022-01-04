package de.themoep.settlements.command.common;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.missions.Mission;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;

public interface MissionCommand<M extends Mission> extends ConfigMessages {

    default void distributeNewMissions(Resident resident, MissionProvider<M> missionProvider) {
        if (missionProvider.getMissionRedistributePrice() > 0) {
            if (!SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), missionProvider.getMissionRedistributePrice()).transactionSuccess()) {
                getError("not-enough-money").tell(resident,
                        "amount", Utils.CURRENCY_FORMAT.format(missionProvider.getMissionRedistributePrice())
                );
                return;
            }
        }
        missionProvider.distributeNewMissions(missionProvider.getMissionTaker(resident));
        getMessage("success").tell(resident,
                "amount", String.valueOf(missionProvider.getMissionTaker(resident).getMissionData().size()),
                "price", Utils.CURRENCY_FORMAT.format(missionProvider.getMissionRedistributePrice())
        );
    }
}
