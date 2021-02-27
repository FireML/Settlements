package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.perk.EmpirePerk;
import me.crz.minecraft.kingdoms.perk.Perk;
import me.crz.minecraft.kingdoms.perk.PerkData;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.resident.ResidentManager;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.logging.Level;

public class EmpireListener implements Listener {
    private final EmpirePerk keepInvPerk;
    private final EmpirePerk keepExpPerk;

    public EmpireListener() {
        keepInvPerk = EmpireManager.getInstance().getPerk("keep-inv");
        if (keepInvPerk == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "keep-inv perk is not defined in empirePerksConfig.yml?");
        }
        keepExpPerk = EmpireManager.getInstance().getPerk("keep-exp");
        if (keepExpPerk == null) {
            KingdomsPlugin.getInstance().getLogger().log(Level.WARNING, "keep-exp perk is not defined in empirePerksConfig.yml?");
        }
    }

    private boolean canAccess(Player player, World world) {
        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(world);
        return empire == null || empire.canAccess(player) || player.hasPermission("kingdoms.bypass.teleport");
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

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Empire e = EmpireManager.getInstance().getEmpireFromWorld(event.getEntity().getWorld());
        if (e != null) {
            if (keepInvPerk != null) {
                PerkData keepInvData = e.getPerkData(keepInvPerk);
                if (keepInvData != null && keepInvData.isActive()) {
                    event.setKeepInventory(true);
                    event.getDrops().clear();
                }
            }
            if (keepExpPerk != null) {
                PerkData keepExpData = e.getPerkData(keepExpPerk);
                if (keepExpData != null && keepExpData.isActive()) {
                    event.setKeepLevel(true);
                    event.setDroppedExp(0);
                }
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
