package me.crz.minecraft.kingdoms.command.village;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.db.MySqlDatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.VillageState;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VillageUnclaimCommand extends VillageSubCommand {

    private Cache<UUID, Runnable> confirm = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public VillageUnclaimCommand(SubCommand parent) {
        super(parent, "unclaim [<radius>]");
        registerCommand(new VillageSubCommand(this, "<radius>", "<#>") {
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
            if (res.hasPermission("kingdoms.command.village.unclaim.radius")) {
                try {
                    int radius = Integer.parseInt(args[0]);
                    int maxRadius = KingdomsPlugin.getInstance().getConfig().getInt("claim-max-radius");
                    if (radius > maxRadius) {
                        getError("radius-too-large").tell(res, "radius", String.valueOf(radius), "max", String.valueOf(maxRadius));
                        return true;
                    }
                    toUnclaim.add(new SimpleChunkLocation(player.getLocation()));
                    toUnclaim.addAll(Utils.getChunksAround(player, radius));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(res, "input", args[0]);
                    return false;
                }
            }
        } else if (args.length == 2) {
            if (res.hasPermission("kingdoms.command.village.unclaim.coords")) {
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
                if (!land.isOwnedBy(res.getVillage())) {
                    if (toUnclaim.size() == 1) {
                        res.tell(getError("not-your-village"), "state", land.getLandOwner().getStateTag());
                        return true;
                    }
                } else if (land.isOutpost()) {
                    if (toUnclaim.size() == 1) {
                        getMessage("outpost").tell(res, "base", base, "outpost", land.getOutpostName());
                        return true;
                    }
                } else if (land.containsHome() && (toUnclaim.size() > 1 || LandManager.getInstance().getClaimedLandSize(res.getVillage()) > 1)) {
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
                unclaimedLand.forEach(l -> getMessage("success").tell(res.getVillage().getOnlineResidents(),
                        "player", res.getName(),
                        "name", res.getName(),
                        "title", res.getTitle(),
                        "rank", res.getVillageRank().getTag(res.getVillage()),
                        "world", l.getWorldName(),
                        "location", l.getSimpleKey(),
                        "centerx", String.valueOf(l.getX() * 16 + 8),
                        "centerz", String.valueOf(l.getZ() * 16 + 8),
                        "x", String.valueOf(l.getX()),
                        "z", String.valueOf(l.getZ()))
                );
                if (res.getVillage().getState() == VillageState.VILLAGE && res.getVillage().getLevel() == 1) {
                    getMessage("new-upkeep").tell(res,
                            "villageupkeep", String.valueOf(res.getVillage().getUpkeep()),
                            "upkeepperclaim", String.valueOf(Utils.getUpkeepCost(res.getVillage().getState(), res.getVillage().getLevel()))
                    );
                }
                LandManager.getInstance().unclaimLand(unclaimedLand);
            };
            if (unclaimedLand.size() == 1) {
                unclaim.run();
            } else {
                confirm.put(res.getUUID(), unclaim);
                getMessage("confirm").tell(res,
                        "command", base + " " + alias + " confirm",
                        "amount", String.valueOf(unclaimedLand.size()),
                        "claims", unclaimedLand.stream().map(l -> l.getX() + "/" + l.getZ()).collect(Collectors.joining(", "))
                );
            }
        } else if (toUnclaim.size() == 1) {
            getError("no-village").tell(res);
        }
        return true;
    }
}
