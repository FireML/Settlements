package me.crz.minecraft.kingdoms.world;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.utils.Message;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.VillageState;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.EventHandler;
import me.crz.minecraft.kingdoms.village.Village;
import org.bukkit.Bukkit;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.VillageManager;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveBlockEvent;
import me.crz.minecraft.kingdoms.Base;

public class WorldListener extends Base {
    public WorldListener() {
        super();
    }

    private boolean canAccess(Player player, World world) {
        if (world.getName().equalsIgnoreCase(KingdomsPlugin.getInstance().getKingdomsWorld()) && !player.hasPermission("kingdoms.staff")) {
            Village village = VillageManager.getInstance().getVillageFromPlayer(player);
            int level = Utils.getKingdomsWorldLevel();
            if (village == null || village.getState() == VillageState.VILLAGE || village.getLevel() < level) {
                Message.get("error.level-required").tell(player, "state", VillageState.KINGDOM.getTag(), "level", String.valueOf(level));
                return false;
            }
        }
        return true;
    }

    @EventHandler
    public void on(PlayerMoveBlockEvent e) {
        if (!canAccess(e.getPlayer(), e.getTo().getWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerTeleportEvent e) {
        if (!canAccess(e.getPlayer(), e.getTo().getWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PlayerChangedWorldEvent e) {
        if (!canAccess(e.getPlayer(), e.getPlayer().getWorld())) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), MessageUtils.replacePlaceholders(Utils.getSpawnCommand(), "player", e.getPlayer().getName()));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!canAccess(event.getPlayer(), event.getPlayer().getWorld())) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), MessageUtils.replacePlaceholders(Utils.getSpawnCommand(), "player", event.getPlayer().getName()));
        }
    }
}
