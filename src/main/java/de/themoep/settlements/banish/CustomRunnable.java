package de.themoep.settlements.banish;

import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;

public interface CustomRunnable {

    interface Player extends CustomRunnable {
        void run(final OwnedLand land, final org.bukkit.entity.Player player, final LandOwner<?, ?> landOwner);
    }

    interface UUID extends CustomRunnable {
        void run(final OwnedLand land, final java.util.UUID u, final LandOwner<?, ?> landOwner);
    }
}
