package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.perk.EmpirePerk;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.ResidentManager;
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
        } else if (hasFlyPerk(event.getPlayer().getWorld())) {
            event.getPlayer().setAllowFlight(true);
            ResidentManager.getInstance().getResident(event.getPlayer()).setHasEmpireFlight(true);
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
        Player player = event.getPlayer();
        Resident resident = ResidentManager.getInstance().getResident(player);
        if (hasFlyPerk(event.getPlayer().getWorld())) {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
                resident.setHasEmpireFlight(true);
            }
        } else if (resident.hasEmpireFlight()
                && player.getAllowFlight()
                && player.getGameMode() != GameMode.CREATIVE
                && player.getGameMode() != GameMode.SPECTATOR
                && !player.hasPermission("kingdoms.bypass.flyremoval")) {
            player.setAllowFlight(false);
            ResidentManager.getInstance().getResident(player).setHasEmpireFlight(false);
            if (player.isFlying()) {
                player.setFlying(false);
            }
        }
    }

    private boolean hasFlyPerk(World world) {
        Empire empireTo = EmpireManager.getInstance().getEmpireFromWorld(world);
        if (empireTo != null) {
            for (Perk perk : empireTo.getPerks()) {
                if (perk instanceof EmpirePerk && ((EmpirePerk) perk).isFlying() && empireTo.getPerkData(perk).isActive()) {
                    return true;
                }
            }
        }
        return false;
    }
}
