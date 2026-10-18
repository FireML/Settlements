package de.themoep.settlements.commandold.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.ChatMap;
import de.themoep.settlements.land.LandManager;

import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.settlement.SettlementManager;
import de.themoep.settlements.settlement.SettlementState;
import de.themoep.settlements.utils.ConfigMessages;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.WorldUtils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.world.SimpleBlockLocation;
import de.themoep.settlements.world.SimpleChunkLocation;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public interface LandOwnerCommand extends ConfigMessages {
    default Settlement getTarget(Resident resident, String arg) {
        Settlement target = null;
        if (arg.startsWith("settlement:") || arg.startsWith("s:")) {
            target = SettlementManager.getInstance().getFromName(arg.split(":")[1]);
            if (target == null) {
                getError("settlement-doesnt-exist").tell(resident, "name", arg.split(":")[1]);
                return null;
            }
        } else {
            String name = arg;
            if (name.contains(":")) {
                name = name.split(":")[1];
            }
            Settlement settlement = SettlementManager.getInstance().getFromName(name);
            if (settlement != null) {
                target = settlement;
            }
            if (target == null) {
                getError("settlement-doesnt-exist").tell(resident, "name", name);
                return null;
            }
        }
        return target;
    }

    default boolean handleClaim(Resident res, Settlement settlement, String... args) {
        Player player = res.getPlayer();
        if (!settlement.canClaim(player.getWorld())) {
            getError("world-not-claimable").tell(player, "world", player.getWorld().getName(), "state", settlement.getStateTag());
            return true;
        }
        int amountClaimed = LandManager.getInstance().getClaimedLandSize(settlement);
        long maxClaimAllowed = settlement.getMaxClaimableChunks();
        Set<SimpleChunkLocation> toClaim = new LinkedHashSet<>();
        if (args.length == 1) {
            if (settlement.hasPermission(res, "claim.radius")) {
                try {
                    int radius = Integer.parseInt(args[0]);
                    int maxRadius = Config.Claiming.MAX_RADIUS;
                    if (radius > maxRadius) {
                        getError("radius-too-large").tell(res, "radius", String.valueOf(radius), "max", String.valueOf(maxRadius));
                        return true;
                    }
                    toClaim.add(new SimpleChunkLocation(player.getLocation()));
                    toClaim.addAll(WorldUtils.getChunksAround(player, radius));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(res, "input", args[0]);
                    return false;
                }
            } else {
                getError("no-permission").tell(res, "permission", "claim.radius");
                return true;
            }
        } else if (args.length >= 2) {
            if (settlement.hasPermission(res, "claim.coords")) {
                try {
                    toClaim.add(new SimpleChunkLocation(player.getWorld().getName(), Integer.parseInt(args[0]), Integer.parseInt(args[1])));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(res, "input", args[0] + " " + args[1]);
                    return false;
                }
            } else {
                getError("no-permission").tell(res, "permission", "claim.coords");
                return true;
            }
        } else {
            toClaim.add(new SimpleChunkLocation(player.getLocation()));
        }

        if (amountClaimed + toClaim.size() > maxClaimAllowed) {
            res.tell(getError("cant-claim-more").getText(), "max", String.valueOf(maxClaimAllowed));
            return true;
        }

        if (amountClaimed > 0) {
            boolean landAround = false;
            for (SimpleChunkLocation c : toClaim) {
                for (SimpleChunkLocation chunk : WorldUtils.getChunksAdjacent(c)) {
                    OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
                    if (checkLand != null && checkLand.isOwnedBy(settlement)) {
                        landAround = true;
                        break;
                    }
                }
            }

            if (!landAround) {
                res.tell(getError("no-land-around").getText());
                return true;
            }
        }

        List<OwnedLand> toSave = new ArrayList<>();
        OUTER:
        for (SimpleChunkLocation c : toClaim) {
            if (amountClaimed >= maxClaimAllowed) {
                getError("cant-claim-more").tell(res, "max", String.valueOf(maxClaimAllowed));
                return true;
            }
            OwnedLand land = LandManager.getInstance().getLand(c);
            if (land != null) {
                if (toClaim.size() > 1) {
                    continue;
                }
                if (land.getSettlement().equals(settlement)) {
                    MessageUtils.tell(player, getError("already-own-land").getComponent());
                } else {
                    MessageUtils.tell(player, getError("cant-claim-here").getReplacedComponent(
                            "owner", land.getSettlement().getName(),
                            "state", land.getSettlement().getStateTag()
                    ));
                }
                return true;
            }
            for (SimpleChunkLocation chunk : WorldUtils.getChunksAround(c, Config.Claiming.ENEMY_DISTANCE)) {
                OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
                if (checkLand == null) {
                    continue;
                }
                if (!checkLand.isOwnedBy(settlement) && (
                        !checkLand.getSettlement().isAlly(settlement) || (
                                checkLand.getSettlement() == null
                                        || !checkLand.getSettlement().isAllowAllyClaim()))) {
                    if (toClaim.size() == 1) {
                        res.tell(getError("claim-too-close").getText(),
                                "owner", checkLand.getSettlement().getName(),
                                "state", checkLand.getSettlement().getStateTag()
                        );
                        return true;
                    } else {
                        continue OUTER;
                    }
                }
            }
            OwnedLand newLand = LandManager.getInstance().claimLand(c, player.getLocation(), settlement);
            MessageUtils.tellResidents(settlement.getOnlineResidents(), getMessage("success").getReplacedComponent(
                    "player", player.getName(),
                    "world", newLand.getWorldName(),
                    "x", String.valueOf(newLand.getX()),
                    "z", String.valueOf(newLand.getZ())
            ));
            if (newLand.containsHome()) {
                SimpleBlockLocation home = new SimpleBlockLocation(newLand.getSettlement().getHome());
                getMessage("home-set").tell(settlement,
                        "title", res.getDisplayTitle(),
                        "displaytitle", res.getDisplayTitle(),
                        "name", player.getName(),
                        "state", settlement.getStateTag(),
                        "world", home.getWorld(),
                        "x", String.valueOf(home.getX()),
                        "y", String.valueOf(home.getY()),
                        "z", String.valueOf(home.getZ()));
            }
            toSave.add(newLand);
            amountClaimed++;
        }
        if (!toSave.isEmpty()) {
            if (settlement.getLevel() == 1 && settlement.getState() == SettlementState.VILLAGE) {
                getMessage("new-upkeep").tell(res,
                        "upkeep", String.valueOf(settlement.getUpkeep()),
                        "upkeepperclaim", String.valueOf(settlement.getClaimUpkeep(player.getWorld().getName()))
                );
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveLand(toSave);
                if (args.length >= 3 && "map".equalsIgnoreCase(args[2])) {
                    new ChatMap(res.getPlayer(), settlement, toSave.getFirst().getMapKey());
                }
            });
        }
        return true;
    }

    Cache<UUID, Runnable> confirm = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).build();

    default boolean handleUnclaim(Resident res, Settlement settlement, String base, String alias, String... args) {
        Player player = res.getPlayer();
        if (!settlement.canClaim(player.getWorld())) {
            getError("world-not-claimable").tell(res, "world", player.getWorld().getName());
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
            if (settlement.hasPermission(res, "unclaim.radius")) {
                try {
                    int radius = Integer.parseInt(args[0]);
                    int maxRadius = Config.Claiming.MAX_RADIUS;
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
            } else {
                getError("no-permission").tell(res, "permission", "unclaim.coords");
                return true;
            }
        } else if (args.length >= 2) {
            if (settlement.hasPermission(res, "unclaim.coords")) {
                try {
                    toUnclaim.add(new SimpleChunkLocation(player.getWorld().getName(), Integer.parseInt(args[0]), Integer.parseInt(args[1])));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(res, "input", args[0] + " " + args[1]);
                    return false;
                }
            } else {
                getError("no-permission").tell(res, "permission", "unclaim.coords");
                return true;
            }
        }

        if (toUnclaim.isEmpty()) {
            toUnclaim.add(new SimpleChunkLocation(player.getLocation()));
        }

        Set<OwnedLand> unclaimedLand = new HashSet<>();
        for (SimpleChunkLocation c : toUnclaim) {
            OwnedLand land = LandManager.getInstance().getLand(c);
            if (land != null) {
                if (!land.isOwnedBy(settlement)) {
                    if (toUnclaim.size() == 1) {
                        getError("not-your-" + settlement.getKey()).tell(res, "state", land.getSettlement().getStateTag());
                        return true;
                    }
                } else if (land.isOutpost()) {
                    if (toUnclaim.size() == 1) {
                        getMessage("outpost").tell(res, "base", base, "outpost", land.getOutpostName());
                        return true;
                    }
                } else if (land.containsHome() && (toUnclaim.size() > 1 || LandManager.getInstance().getClaimedLandSize(settlement) > 1)) {
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
                unclaimedLand.forEach(l -> getMessage("success").tell(settlement,
                        "player", res.getName(),
                        "name", res.getName(),
                        "title", res.getDisplayTitle(),
                        "rank", settlement.getRank(res).getTag(),
                        "world", l.getWorldName(),
                        "location", l.getSimpleKey(),
                        "centerx", String.valueOf(l.getX() * 16 + 8),
                        "centerz", String.valueOf(l.getZ() * 16 + 8),
                        "x", String.valueOf(l.getX()),
                        "z", String.valueOf(l.getZ()))
                );
                if (args.length >= 3 && "map".equalsIgnoreCase(args[2])) {
                    ConfigMessages configMessages = unclaimedLand.iterator().next().getMapKey();
                    Utils.runAsync(() -> new ChatMap(res.getPlayer(), settlement, configMessages));
                }
                LandManager.getInstance().unclaimLand(unclaimedLand);
                if (settlement.getLevel() == 1) {
                    getMessage("new-upkeep").tell(res,
                            "upkeep", String.valueOf(settlement.getUpkeep()),
                            "upkeepperclaim", String.valueOf(settlement.getClaimUpkeep(player.getWorld().getName()))
                    );
                }

            };
            if (unclaimedLand.size() > 1 || (args.length >= 3 && "map".equalsIgnoreCase(args[2]))) {
                confirm.put(res.getUUID(), unclaim);
                getMessage("confirm").tell(res,
                        "command", base + " " + alias + " confirm",
                        "amount", String.valueOf(unclaimedLand.size()),
                        "claims", unclaimedLand.stream().map(l -> l.getX() + "/" + l.getZ()).collect(Collectors.joining(", "))
                );
            } else {
                unclaim.run();
            }
        } else if (toUnclaim.size() == 1) {
            getError("no-" + settlement.getKey()).tell(res);
        }
        return true;
    }
}
