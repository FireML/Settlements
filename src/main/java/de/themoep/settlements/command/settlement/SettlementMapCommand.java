package de.themoep.settlements.command.settlement;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.settlements.SettlementsPlugin;
import de.themoep.settlements.command.ResidentSubCommand;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.customevents.PlayerMoveChunkEvent;
import de.themoep.settlements.land.ChatMap;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.resident.ResidentManager;
import de.themoep.settlements.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SettlementMapCommand extends ResidentSubCommand implements Listener {
    private Cache<UUID, Boolean> autoMap = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    public SettlementMapCommand(SubCommand parent) {
        super(parent, "map [auto]");
        SettlementsPlugin.getInstance().registerListener(this);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        Player player = resident.getPlayer();
        if ((resident.hasSettlement() && !resident.getSettlement().canClaim(player.getWorld())) || !Utils.isWorldClaimable(player.getWorld())) {
            getError("world-not-claimable").tell(resident);
            return true;
        }

        Utils.runAsync(() -> new ChatMap(player, resident.getSettlement(), this));
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
            Utils.runAsync(() -> {
                Resident resident = ResidentManager.getInstance().getResident(event.getPlayer());
                new ChatMap(event.getPlayer(), resident.getSettlement(), this);
            });
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
