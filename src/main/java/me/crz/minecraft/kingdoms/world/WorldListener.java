package me.crz.minecraft.kingdoms.world;

import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveBlockEvent;
import me.crz.minecraft.kingdoms.Base;

public class WorldListener extends Base
{
    public WorldListener() {
        super();
    }
    
    @EventHandler
    public void on(final PlayerMoveBlockEvent e) {
        if (e.getFrom().getWorld().getName().equalsIgnoreCase("kingdom")) {
            final Village village = VillageManager.getInstance().getVillageFromPlayer(e.getPlayer());
            if (village.getLevel() < 4 && !e.getPlayer().isOp()) {
                e.setCancelled(true);
                MessageUtils.tell(e.getPlayer(), "&cYou must be kingdom level 4 to be here!");
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + e.getPlayer().getName());
            }
        }
    }
    
    @EventHandler
    public void on(final PlayerChangedWorldEvent e) {
        if (e.getFrom().getName().equalsIgnoreCase("kingdom")) {
            final Village village = VillageManager.getInstance().getVillageFromPlayer(e.getPlayer());
            if (village.getLevel() < 4 && !e.getPlayer().isOp()) {
                MessageUtils.tell(e.getPlayer(), "&cYou must be kingdom level 4 to be here!");
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + e.getPlayer().getName());
            }
        }
    }
    
    @EventHandler
    public void on(final PlayerTeleportEvent e) {
        if (e.getFrom().getWorld().getName().equalsIgnoreCase("kingdom")) {
            final Village village = VillageManager.getInstance().getVillageFromPlayer(e.getPlayer());
            if (village.getLevel() < 4 && !e.getPlayer().isOp()) {
                e.setCancelled(true);
                MessageUtils.tell(e.getPlayer(), "&cYou must be kingdom level 4 to be here!");
                Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + e.getPlayer().getName());
            }
        }
    }
}
