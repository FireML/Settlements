package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.perk.EmpirePerk;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EmpireListener implements Listener {

    private boolean canAccess(Player player, World world) {
        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(world);
        return empire == null || empire.canAccess(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!canAccess(event.getPlayer(), event.getPlayer().getWorld())) {
            Utils.teleportToSpawn(event.getPlayer());
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!canAccess(event.getPlayer(), event.getTo().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        Empire empireTo = EmpireManager.getInstance().getEmpireFromWorld(event.getPlayer().getWorld());
        Player player = event.getPlayer();
        boolean removeFlying = true;
        if (empireTo != null) {
            for (Perk perk : empireTo.getPerks()) {
                if (perk instanceof EmpirePerk && ((EmpirePerk) perk).isFlying() && empireTo.getPerkData(perk).isActive()) {
                    if (!player.getAllowFlight()) {
                        player.setAllowFlight(true);
                    }
                    removeFlying = false;
                    break;
                }
            }
        }
        if (removeFlying
                && player.getAllowFlight()
                && player.getGameMode() != GameMode.CREATIVE
                && player.getGameMode() != GameMode.SPECTATOR
                && !player.hasPermission("kingdoms.staff")) {
            player.setAllowFlight(false);
            if (player.isFlying()) {
                player.setFlying(false);
            }
        }
    }
}
