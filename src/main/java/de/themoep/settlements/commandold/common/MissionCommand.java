package de.themoep.settlements.commandold.common;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.missions.Mission;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;
import uk.firedev.daisylib.utils.Scheduling;

import java.util.ArrayList;
import java.util.List;

public interface MissionCommand<M extends Mission> extends ConfigMessages {

    List<Resident> confirmation = new ArrayList<>();

    default void distributeNewMissions(Resident resident, Settlement missionProvider) {

        if (!confirmation.contains(resident)) {
            confirmation.add(resident);
            getMessage("redistribute.confirm").tell(resident,
                    "price", Utils.CURRENCY_FORMAT.format(missionProvider.getMissionRedistributePrice())
            );
            Scheduling.runTaskLater(() -> confirmation.remove(resident), 100L);
            return;
        }

         confirmation.remove(resident);

        if (missionProvider.getMissionRedistributePrice() > 0) {
            if (!SettlementsPlugin.getInstance().getEconomy().withdrawPlayer(resident.getPlayer(), missionProvider.getMissionRedistributePrice()).transactionSuccess()) {
                getError("not-enough-money").tell(resident,
                        "amount", Utils.CURRENCY_FORMAT.format(missionProvider.getMissionRedistributePrice())
                );
                return;
            }
        }

        int distributed = missionProvider.distributeNewMissions(resident);
        if (distributed > 0) {
            getMessage("redistribute.success").tell(resident,
                    "amount", String.valueOf(distributed),
                    "price", Utils.CURRENCY_FORMAT.format(missionProvider.getMissionRedistributePrice())
            );
        } else {
            if (missionProvider.getMissionRedistributePrice() > 0) {
                SettlementsPlugin.getInstance().getEconomy().depositPlayer(resident.getPlayer(), missionProvider.getMissionRedistributePrice());
            }
            if (distributed < 0 || resident.getMissionData().isEmpty()) {
                getError("not-unlocked").tell(resident,
                        "state", missionProvider.getStateTag(),
                        "name", missionProvider.getName(),
                        "function", "missions"
                );
            } else {
                getMessage("redistribute.fail").tell(resident,
                        "state", missionProvider.getStateTag(),
                        "name", missionProvider.getName()
                );
            }
        }
    }
}
