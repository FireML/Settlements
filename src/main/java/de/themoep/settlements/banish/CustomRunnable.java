package de.themoep.settlements.banish;

import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.settlement.Settlement;

public interface CustomRunnable {

    interface Player extends CustomRunnable {
        void run(final OwnedLand land, final org.bukkit.entity.Player player, final Settlement settlement);
    }

    interface UUID extends CustomRunnable {
        void run(final OwnedLand land, final java.util.UUID u, final Settlement settlement);
    }
}
