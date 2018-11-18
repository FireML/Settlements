package me.crz.minecraft.kingdoms.command.empire;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.PlayerSubCommand;
import me.crz.minecraft.kingdoms.empire.EmpireManager;
import me.crz.minecraft.kingdoms.essentials.events.PlayerMoveChunkEvent;
import me.crz.minecraft.kingdoms.land.ChatMap;
import me.crz.minecraft.kingdoms.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.crz.minecraft.kingdoms.command.SubCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EmpireMapCommand extends PlayerSubCommand implements Listener {
    private Cache<UUID, Boolean> autoMap = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    public EmpireMapCommand(SubCommand parent) {
        super(parent, "map [auto]");
        setPermission("kingdoms.user");
        Bukkit.getPluginManager().registerEvents(this, KingdomsPlugin.getInstance());
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (EmpireManager.getInstance().getEmpireFromWorld(player.getWorld()) == null) {
            getError("not-in-empire-world").tell(player);
            return true;
        }

        Utils.runAsync(() -> new ChatMap(player, this));
        if (args.length > 0 && "auto".equalsIgnoreCase(args[0])) {
            boolean enable = autoMap.getIfPresent(player.getUniqueId()) == null;
            if (enable) {
                autoMap.put(player.getUniqueId(), true);
            } else {
                autoMap.invalidate(player.getUniqueId());
            }
        }
        return true;
    }

    @EventHandler
    public void onChunkChange(PlayerMoveChunkEvent event) {
        if (autoMap.getIfPresent(event.getPlayer().getUniqueId()) != null) {
            Utils.runAsync(() -> new ChatMap(event.getPlayer(), this));
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        autoMap.invalidate(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        autoMap.invalidate(event.getPlayer().getUniqueId());
    }


}
