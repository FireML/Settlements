package de.themoep.settlements.banish;

import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.OwnedLand;
import org.bukkit.entity.Player;

public interface CustomRunnable {
    void run(final OwnedLand land, final Player player, final LandOwner landOwner);
}
