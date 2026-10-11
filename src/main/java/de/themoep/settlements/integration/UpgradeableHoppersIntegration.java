package de.themoep.settlements.integration;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.utils.Utils;
import me.angeschossen.upgradeablehoppers.api.events.hopper.HopperPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import de.themoep.settlements.utils.ComponentUtils;

public class UpgradeableHoppersIntegration implements Listener {
    @EventHandler
    public void onHopperPlace(HopperPlaceEvent e) {
        Location hopperLoc = e.getHopper().getPosition().toLocation();
        Player p = Bukkit.getPlayer(e.getHopperPlayer().getUUID());
        if (p != null && (Utils.isWorldClaimable(hopperLoc.getWorld()) && !LandManager.getInstance().isLandClaimed(hopperLoc))) {
            e.setCancelled(true);
            p.sendMessage(ComponentUtils.parseComponent("&cYou cannot place Chunk Hoppers outside of claims."));
        }
    }
}