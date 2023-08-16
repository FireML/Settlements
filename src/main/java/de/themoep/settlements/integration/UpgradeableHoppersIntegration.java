package de.themoep.settlements.integration;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.config.Config;
import me.angeschossen.upgradeablehoppers.api.events.HopperPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UpgradeableHoppersIntegration implements Listener {
    @EventHandler
    public void onHopperPlace(HopperPlaceEvent e) {
        Location hopperLoc = e.getHopper().getLocation();
        Player p = Bukkit.getPlayer(e.getHopperPlayer().getUID());
        if (p != null && !Config.Claiming.DISABLED_WORLDS.contains(hopperLoc.getWorld().getName()) && !LandManager.getInstance().isLandClaimed(hopperLoc)) {
            e.setCancelled(true);
            p.sendMessage(MessageUtils.toComponent("&cYou cannot place Chunk Hoppers outside of claims."));
        }
    }
}