package me.crz.minecraft.kingdoms.banish;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveChunkEvent;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.village.VillageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BanishListener implements Listener {
    
    @EventHandler
    public void on(PlayerMoveChunkEvent e) {
        VillageManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getTo()), e.getPlayer(), (land, p, village) -> {
            sendInfo(p, village, "move");
            e.setCancelled(true);
            Location to = e.getPlayer().getLocation().add(e.getPlayer().getLocation().getDirection().setY(0).multiply(-1));
            Bukkit.getScheduler().runTask(KingdomsPlugin.getInstance(), () -> e.getPlayer().teleport(to));
        });
    }

    @EventHandler
    public void on(PlayerTeleportEvent e) {
        VillageManager.getInstance().isResBanished(LandManager.getInstance().getLand(e.getTo()), e.getPlayer(), (land, p, village) -> {
            sendInfo(p, village, "teleport");
            e.setCancelled(true);
            Bukkit.getScheduler().runTask(KingdomsPlugin.getInstance(), () -> Utils.teleportToSpawn(p));
        });
    }

    private void sendInfo(Player p, Village village, String type) {
        String[] replacements = new String[] {
                "state", village.getState().getTag(),
                "name", village.getName()
        };
        MessageUtils.tellAdvanced(p, "banished." + type, replacements);
    }
}
