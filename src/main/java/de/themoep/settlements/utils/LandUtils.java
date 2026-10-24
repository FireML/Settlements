package de.themoep.settlements.utils;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.config.message.ConfigMessage;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class LandUtils {

    public static boolean checkPlotAdmin(@NonNull Resident resident, @NonNull Chunk chunk) {
        OwnedLand land = LandManager.getInstance().getLand(chunk);
        if (land == null) {
            ConfigMessage.getError("not-a-claim").send(resident); // TODO message
            return false;
        }
        return checkPlotAdmin(resident, land);
    }

    public static boolean checkPlotAdmin(@NonNull Player player, @NonNull OwnedLand land) {
        Resident resident = ResidentManager.getInstance().getResident(player);
        return checkPlotAdmin(resident, land);
    }

    public static boolean checkPlotAdmin(@NonNull Resident resident, @NonNull OwnedLand land) {
        if (land.canAdmin(resident)) {
            return true;
        }
        if (land.isPlotOwner(resident)) {
            return true;
        }
        if (land.isOwnedBy(resident) || land.isEmbassy()) {
            ConfigMessage.getError("not-your-plot").send(resident);
        } else {
            ConfigMessage.getError("not-your-settlement")
                .replace("state", land.getSettlement().getStateTag())
                .send(resident);
        }
        return false;
    }

    private boolean isPlotAdmin(@NonNull Resident resident) {
        return resident.hasSettlement() && resident.getSettlement().canAdminPlots(resident);
    }

}
