package me.crz.minecraft.kingdoms.banish;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.customevents.PlayerMoveChunkEvent;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.LandOwner;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BanishListener implements Listener {

    @EventHandler
    public void on(PlayerMoveChunkEvent e) {
        ResidentManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getTo()), e.getPlayer(), (land, p, landOwner) -> {
            if (!e.getPlayer().hasPermission("kingdoms.bypass.banish")) {
                sendInfo(p, landOwner, "move");
                e.setCancelled(true);
                Location to = e.getPlayer().getLocation().add(e.getPlayer().getLocation().getDirection().setY(0).multiply(-1));
                Bukkit.getScheduler().runTask(KingdomsPlugin.getInstance(), () -> e.getPlayer().teleport(to));
            }
        });
    }

    @EventHandler
    public void on(PlayerTeleportEvent e) {
        ResidentManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getTo()), e.getPlayer(), (land, p, landOwner) -> {
            sendInfo(p, landOwner, "teleport");
            if (!e.getPlayer().hasPermission("kingdoms.bypass.banish")) {
                e.setCancelled(true);
                Bukkit.getScheduler().runTask(KingdomsPlugin.getInstance(), () -> Utils.teleportToSpawn(p));
            }
        });
    }

    @EventHandler
    public void on(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTask(KingdomsPlugin.getInstance(), () -> ResidentManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getPlayer().getLocation()), e.getPlayer(), (land, p, landOwner) -> {
            sendInfo(p, landOwner, "join");
            if (!e.getPlayer().hasPermission("kingdoms.bypass.banish")) {
                Utils.teleportToSpawn(p);
            }
        }));
    }

    private void sendInfo(Player p, LandOwner landOwner, String type) {
        String[] replacements = new String[] {
                "state", landOwner.getStateTag(),
                "name", landOwner.getName()
        };
        MessageUtils.tellAdvanced(p, "banished." + type, replacements);
    }

}
