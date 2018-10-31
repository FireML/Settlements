package me.crz.minecraft.kingdoms.empire;

import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EmpireListener implements Listener {

    private boolean canAccess(Player player, World world) {
        Empire empire = EmpireManager.getInstance().getEmpireFromWorld(world);
        return empire == null || empire.canAccess(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!canAccess(event.getPlayer(), event.getPlayer().getWorld())) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), MessageUtils.replacePlaceholders(Utils.getSpawnCommand(), "player", event.getPlayer().getName()));
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!canAccess(event.getPlayer(), event.getTo().getWorld())) {
            event.setCancelled(true);
        }
    }
}
