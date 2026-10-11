package de.themoep.settlements.banish;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.customevents.PlayerMoveChunkEvent;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.events.LandPreClaimEvent;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import uk.firedev.daisylib.utils.Scheduling;

public class BanishListener implements Listener {

    @EventHandler
    public void on(PlayerMoveChunkEvent e) {
        ResidentManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getTo()), e.getPlayer(), (land, p, landOwner) -> {
            if (!e.getPlayer().hasPermission("settlements.bypass.banish")) {
                sendInfo(p, landOwner, "move");
                e.setCancelled(true);
                Location to = e.getPlayer().getLocation().add(e.getPlayer().getLocation().getDirection().setY(0).multiply(-1));
                e.getPlayer().teleportAsync(to);
            }
        });
    }

    @EventHandler
    public void on(PlayerTeleportEvent e) {
        ResidentManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getTo()), e.getPlayer(), (land, p, landOwner) -> {
            sendInfo(p, landOwner, "teleport");
            if (!e.getPlayer().hasPermission("settlements.bypass.banish")) {
                e.setCancelled(true);
                if (!e.getTo().getChunk().equals(e.getTo().getChunk())) {
                    Utils.teleportToSpawn(p);
                }
            }
        });
    }

    @EventHandler
    public void on(PlayerSpawnLocationEvent e) {
        ResidentManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getSpawnLocation()), e.getPlayer(), (land, p, landOwner) -> {
            Utils.runLater(() -> sendInfo(p, landOwner, "join"), 10);
            if (!e.getPlayer().hasPermission("settlements.bypass.banish")) {
                e.setSpawnLocation(e.getSpawnLocation().getWorld().getSpawnLocation());
            }
        });
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        Utils.runLater( () -> ResidentManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getPlayer().getLocation()), e.getPlayer(), (land, p, landOwner) -> {
            sendInfo(p, landOwner, "join");
            if (!e.getPlayer().hasPermission("settlements.bypass.banish") && !e.getPlayer().getChunk().equals(e.getPlayer().getWorld().getSpawnLocation().getChunk())) {
                Utils.teleportToSpawn(p);
            }
        }), 1);
    }

    @EventHandler
    public void on(LandPreClaimEvent e) {
        for (Player pl : e.getLand().getPlayers()) {
            ResidentManager.getInstance().isResBanished(e.getLand(), pl, (land, p, landOwner) -> {
            Utils.runLater(() -> sendInfo(p, landOwner, "join"), 10);
                if (!pl.hasPermission("settlements.bypass.banish")) {
                    Bukkit.getServer().dispatchCommand(pl, "cmi spawn");
                }
            });
        }
    }

    private void sendInfo(Player p, LandOwner landOwner, String type) {
        String[] replacements = new String[] {
                "state", landOwner.getStateTag(),
                "name", landOwner.getName()
        };
        MessageUtils.tellAdvanced(p, "banished." + type, replacements);
    }

}
