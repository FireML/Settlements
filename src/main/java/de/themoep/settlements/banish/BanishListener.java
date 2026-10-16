package de.themoep.settlements.banish;

import uk.firedev.daisylib.events.move.PlayerMoveChunkEvent;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.LandOwner;
import de.themoep.settlements.land.events.LandPreClaimEvent;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

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
    public void on(AsyncPlayerSpawnLocationEvent e) {
        UUID uuid = e.getConnection().getProfile().getId();
        if (uuid == null) {
            return;
        }
        User user = LuckPermsProvider.get()
            .getUserManager()
            .loadUser(uuid).join(); // Safe to block - We're off the main thread.
        boolean hasBypass = user.getCachedData()
            .getPermissionData()
            .checkPermission("settlements.bypass.banish")
            .asBoolean();

        ResidentManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getSpawnLocation()), uuid, (_, u, landOwner) -> {
            Utils.runLater(() -> sendInfo(u, landOwner, "join"), 10);
            if (!hasBypass) {
                e.setSpawnLocation(e.getSpawnLocation().getWorld().getSpawnLocation());
            }
        });
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        Utils.runLater(() -> ResidentManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getPlayer().getLocation()), e.getPlayer(), (land, p, landOwner) -> {
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

    private void sendInfo(Player p, LandOwner<?, ?> landOwner, String type) {
        String[] replacements = new String[] {
                "state", landOwner.getStateTag(),
                "name", landOwner.getName()
        };
        MessageUtils.tellAdvanced(p, "banished." + type, replacements);
    }

    private void sendInfo(UUID u, LandOwner<?, ?> landOwner, String type) {
        Player player = Bukkit.getPlayer(u);
        if (player == null) {
            return;
        }
        sendInfo(player, landOwner, type);
    }

}
