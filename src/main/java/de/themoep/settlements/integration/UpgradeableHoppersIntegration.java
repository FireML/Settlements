package de.themoep.settlements.integration;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import uk.firedev.poleislib.libs.angeschossen.upgradeablehoppers.api.events.hopper.HopperPlaceEvent;

public class UpgradeableHoppersIntegration implements Listener {
    @EventHandler
    public void onHopperPlace(HopperPlaceEvent e) {
        Location hopperLoc = e.getHopper().getLocation();
        Player p = Bukkit.getPlayer(e.getHopperPlayer().getUUID());
        if (p != null && !Config.Claiming.DISABLED_WORLDS.contains(hopperLoc.getWorld().getName()) && !LandManager.getInstance().isLandClaimed(hopperLoc)) {
            e.setCancelled(true);
            p.sendMessage("§cYou cannot place Chunk Hoppers outside of claims.");
        }
    }
}