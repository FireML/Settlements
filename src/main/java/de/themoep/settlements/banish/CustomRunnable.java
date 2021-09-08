package de.themoep.settlements.banish;

import de.themoep.settlements.land.LandOwner;
import org.bukkit.entity.Player;
import de.themoep.settlements.land.OwnedLand;

public interface CustomRunnable {
    void run(final OwnedLand land, final Player player, final LandOwner landOwner);
}
