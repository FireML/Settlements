package de.themoep.settlements.command.settlement;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.land.ChatMap;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.portal.Portal;
import de.themoep.settlements.portal.PortalManager;
import de.themoep.settlements.utils.WorldUtils;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.world.SimpleChunkLocation;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SettlementUnclaimCommand extends SettlementSubCommand {

    private Cache<UUID, Runnable> confirm = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public SettlementUnclaimCommand(SubCommand parent) {
        super(parent, "unclaim [<radius>]");
        registerCommand(new SettlementSubCommand(this, "<radius>", "<#>") {
            @Override
            public boolean run(Resident resident, String base, String alias, String... args) {
                return false;
            }
        });
    }

    @Override
    public boolean run(Resident res, String base, String alias, String... args) {
        Player player = res.getPlayer();
        if (!Utils.isWorldClaimable(player.getWorld())) {
            res.tell(getError("world-not-claimable"), "world", player.getWorld().getName());
            return true;
        }

        Set<SimpleChunkLocation> toUnclaim = new LinkedHashSet<>();
        if (args.length == 1) {
            if ("confirm".equalsIgnoreCase(args[0])) {
                Runnable run = confirm.getIfPresent(res.getUUID());
                confirm.invalidate(res.getUUID());
                if (run != null) {
                    run.run();
                } else {
                    getError("nothing-to-confirm").tell(res);
                }
                return true;
            }
            if (res.hasPermission("settlements.command.settlement.unclaim.radius")) {
                try {
                    int radius = Integer.parseInt(args[0]);
                    int maxRadius = Config.Settlement.Claiming.MAX_RADIUS;
                    if (radius > maxRadius) {
                        getError("radius-too-large").tell(res, "radius", String.valueOf(radius), "max", String.valueOf(maxRadius));
                        return true;
                    }
                    toUnclaim.add(new SimpleChunkLocation(player.getLocation()));
                    toUnclaim.addAll(WorldUtils.getChunksAround(player, radius));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(res, "input", args[0]);
                    return false;
                }
            }
        } else if (args.length >= 2) {
            if (res.hasPermission("settlements.command.settlement.unclaim.coords")) {
                try {
                    toUnclaim.add(new SimpleChunkLocation(player.getWorld().getName(), Integer.parseInt(args[0]), Integer.parseInt(args[1])));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(res, "input", args[0] + " " + args[1]);
                    return false;
                }
            }
        }

        if (toUnclaim.isEmpty()) {
            toUnclaim.add(new SimpleChunkLocation(player.getLocation()));
        }

        Set<OwnedLand> unclaimedLand = new HashSet<>();
        for (SimpleChunkLocation c : toUnclaim) {
            OwnedLand land = LandManager.getInstance().getLand(c);
            if (land != null) {
                if (!land.isOwnedBy(res.getSettlement())) {
                    if (toUnclaim.size() == 1) {
                        res.tell(getError("not-your-settlement"), "state", land.getLandOwner().getStateTag());
                        return true;
                    }
                } else if (land.isOutpost()) {
                    if (toUnclaim.size() == 1) {
                        getMessage("outpost").tell(res, "base", base, "outpost", land.getOutpostName());
                        return true;
                    }
                } else if (land.containsHome() && (toUnclaim.size() > 1 || LandManager.getInstance().getClaimedLandSize(res.getSettlement()) > 1)) {
                    if (toUnclaim.size() == 1) {
                        getMessage("home").tell(res, "base", base);
                        return true;
                    }
                } else {
                    unclaimedLand.add(land);
                    unclaimedLand.addAll(LandManager.getInstance().getAllConnectedLand(land, search -> {
                        if (search.isOutpost()) {
                            return LandManager.FilterResponse.SKIP_BRANCH;
                        } else if (search.containsHome()) {
                            return LandManager.FilterResponse.SKIP_BRANCH;
                        }
                        return LandManager.FilterResponse.ACCEPT;
                    }));
                }
            }
        }
        if (!unclaimedLand.isEmpty()) {
            Runnable unclaim = () -> {
                unclaimedLand.forEach(l -> getMessage("success").tell(res.getSettlement().getOnlineResidents(),
                        "player", res.getName(),
                        "name", res.getName(),
                        "title", res.getSettlementTitle(),
                        "displaytitle", res.getDisplayTitle(),
                        "rank", res.getSettlementRank().getTag(res.getSettlement()),
                        "world", l.getWorldName(),
                        "location", l.getSimpleKey(),
                        "centerx", String.valueOf(l.getX() * 16 + 8),
                        "centerz", String.valueOf(l.getZ() * 16 + 8),
                        "x", String.valueOf(l.getX()),
                        "z", String.valueOf(l.getZ()))
                );
                if (res.getSettlement().getState() == SettlementState.VILLAGE && res.getSettlement().getLevel() == 1) {
                    getMessage("new-upkeep").tell(res,
                            "settlementupkeep", String.valueOf(res.getSettlement().getUpkeep()),
                            "upkeepperclaim", String.valueOf(res.getSettlement().getClaimUpkeep(player.getWorld().getName()))
                    );
                }
                if (args.length >= 3 && "map".equalsIgnoreCase(args[2])) {
                    ConfigMessages configMessages = unclaimedLand.iterator().next().getMapKey();
                    Utils.runAsync(() -> new ChatMap(res.getPlayer(), configMessages));
                }
                LandManager.getInstance().unclaimLand(unclaimedLand);

            };
            Set<Portal> portals = new HashSet<>();
            if (Config.Portal.ENABLED) {
                for (OwnedLand land : unclaimedLand) {
                    portals.addAll(PortalManager.getInstance().getPortals(land));
                }
            }
            if (unclaimedLand.size() > 1 || !portals.isEmpty() || (args.length >= 3 && "map".equalsIgnoreCase(args[2]))) {
                confirm.put(res.getUUID(), unclaim);
                getMessage("confirm").tell(res,
                        "command", base + " " + alias + " confirm",
                        "amount", String.valueOf(unclaimedLand.size()),
                        "claims", unclaimedLand.stream().map(l -> l.getX() + "/" + l.getZ()).collect(Collectors.joining(", ")),
                        "portal-amount", String.valueOf(portals.size()),
                        "portals", portals.stream().map(Portal::getLocation).map(l -> l.getX() + "/" + l.getY() + "/" + l.getZ()).collect(Collectors.joining(", "))
                );
            } else {
                unclaim.run();
            }
        } else if (toUnclaim.size() == 1) {
            getError("no-settlement").tell(res);
        }
        return true;
    }
}
