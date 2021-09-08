package de.themoep.settlements.command.village.outpost;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.command.village.VillageSubCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.config.Config;
import org.bukkit.Chunk;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class OutpostUnclaimCommand extends VillageSubCommand {

    private Cache<UUID, Runnable> confirm = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public OutpostUnclaimCommand(VillageSubCommand parent) {
        super(parent, "unclaim");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (args.length == 1 && "confirm".equalsIgnoreCase(args[0])) {
            Runnable run = confirm.getIfPresent(resident.getUUID());
            if (run != null) {
                confirm.invalidate(resident.getUUID());
                run.run();
            } else {
                getError("nothing-to-confirm").tell(resident);
            }
            return true;
        }

        Chunk currentChunk = resident.getPlayer().getLocation().getChunk();
        OwnedLand land = LandManager.getInstance().getLand(currentChunk);
        if (land == null || !land.isOwnedBy(resident.getVillage())) {
            resident.tell(getError("not-your-village"), "state", resident.getVillage().getState().getTag());
            return true;
        }
        if (!land.isOutpost()) {
            resident.tell(getError("not-an-outpost"));
            return true;
        }

        AtomicBoolean connectedToOutpost = new AtomicBoolean(false);
        AtomicBoolean connectedToHome = new AtomicBoolean(false);
        Collection<OwnedLand> toUnclaim = LandManager.getInstance().getAllConnectedLand(land, search -> {
            if (search.isOutpost()) {
                // skip branch if this is connected to another outpost chunk
                connectedToOutpost.set(true);
                return LandManager.FilterResponse.SKIP_BRANCH;
            }
            if (search.containsHome()) {
                // skip branch if this is connected to the home chunk
                connectedToHome.set(true);
                return LandManager.FilterResponse.SKIP_BRANCH;
            }
            return LandManager.FilterResponse.ACCEPT;
        });

        if (connectedToHome.get()) {
            getMessage("connected-to-home").tell(resident);
        } else if (connectedToOutpost.get()) {
            getMessage("connected-to-outpost").tell(resident);
        }

        toUnclaim.add(land);
        Runnable unclaim = () -> {
            LandManager.getInstance().unclaimLand(toUnclaim);
            MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("success"),
                    "amount", String.valueOf(toUnclaim.size() - 1),
                    "x", String.valueOf(resident.getPlayer().getLocation().getBlockX()),
                    "z", String.valueOf(resident.getPlayer().getLocation().getBlockZ()));
        };
        Set<Portal> portals = new HashSet<>();
        if (Config.Portal.ENABLED) {
            for (OwnedLand ownedLand : toUnclaim) {
                portals.addAll(PortalManager.getInstance().getPortals(ownedLand));
            }
        }
        if (toUnclaim.size() == 1 && portals.isEmpty()) {
            unclaim.run();
        } else {
            confirm.put(resident.getUUID(), unclaim);
            getMessage("confirm").tell(resident,
                    "command", base + " " + alias + " confirm",
                    "amount", String.valueOf(toUnclaim.size()),
                    "claims", toUnclaim.stream().map(l -> l.getX() + "/" + l.getZ()).collect(Collectors.joining(", ")),
                    "portal-amount", String.valueOf(portals.size()),
                    "portals", portals.stream().map(Portal::getLocation).map(l -> l.getX() + "/" + l.getY() + "/" + l.getZ()).collect(Collectors.joining(", "))
            );
        }
        return true;
    }
}
