package de.themoep.settlements.empire;

import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.perk.EmpirePerk;
import de.themoep.settlements.perk.Perk;
import de.themoep.settlements.perk.PerkData;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
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
            SettlementsPlugin.getInstance().getLogging().warn("keep-inv perk is not defined in empirePerksConfig.yml?");
        }
        keepExpPerk = EmpireManager.getInstance().getPerk("keep-exp");
        if (keepExpPerk == null) {
            SettlementsPlugin.getInstance().getLogging().warn("keep-exp perk is not defined in empirePerksConfig.yml?");
        }
    }

    private boolean canAccess(Player player, World world) {
        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(world);
        return empire == null || empire.canAccess(player) || player.hasPermission("settlements.bypass.teleport");
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
                && !player.hasPermission("settlements.bypass.flyremoval")) {
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
