package de.themoep.settlements.command.common;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.missions.Mission;
import de.themoep.settlements.missions.MissionProvider;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public interface MissionCommand<M extends Mission> extends ConfigMessages {

    List<Resident> confirmation = new ArrayList<>();

    default void distributeNewMissions(Resident resident, MissionProvider<M> missionProvider) {

        if (!confirmation.contains(resident)) {
            confirmation.add(resident);
            getMessage("redistribute.confirm").tell(resident,
                    "price", Utils.CURRENCY_FORMAT.format(missionProvider.getMissionRedistributePrice())
            );
            new BukkitRunnable() {
                @Override
                public void run() {
                    confirmation.remove(resident);
                }
            }.runTaskLater(SettlementsPlugin.getInstance(), 100L);
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

        int distributed = missionProvider.distributeNewMissions(missionProvider.getMissionTaker(resident));
        if (distributed > 0) {
            getMessage("redistribute.success").tell(resident,
                    "amount", String.valueOf(distributed),
                    "price", Utils.CURRENCY_FORMAT.format(missionProvider.getMissionRedistributePrice())
            );
        } else {
            if (missionProvider.getMissionRedistributePrice() > 0) {
                SettlementsPlugin.getInstance().getEconomy().depositPlayer(resident.getPlayer(), missionProvider.getMissionRedistributePrice());
            }
            if (distributed < 0 || missionProvider.getMissionTaker(resident).getMissionData().isEmpty()) {
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
