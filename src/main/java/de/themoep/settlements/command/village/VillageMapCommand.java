package de.themoep.settlements.command.village;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.settlements.customevents.PlayerMoveChunkEvent;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.PlayerSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.ChatMap;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VillageMapCommand extends PlayerSubCommand implements Listener {
    private Cache<UUID, Boolean> autoMap = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    public VillageMapCommand(SubCommand parent) {
        super(parent, "map [auto]");
        SettlementsPlugin.getInstance().registerListener(this);
    }

    @Override
    public boolean run(Player player, String base, String alias, String... args) {
        if (!Utils.isWorldClaimable(player.getWorld())) {
            getError("world-not-claimable").tell(player);
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
