package de.themoep.settlements.command.village;

import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.village.Village;
import de.themoep.settlements.village.VillageState;
import de.themoep.settlements.command.SubCommand;
import de.themoep.settlements.db.DatabaseManager;
import de.themoep.settlements.land.ChatMap;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.utils.WorldUtils;
import de.themoep.settlements.utils.MessageUtils;
import de.themoep.settlements.utils.Utils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.world.SimpleBlockLocation;
import de.themoep.settlements.world.SimpleChunkLocation;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class VillageClaimCommand extends VillageSubCommand {
    public VillageClaimCommand(SubCommand parent) {
        super(parent, "claim [<radius>]");
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
        Village village = res.getVillage();
        int amountClaimed = LandManager.getInstance().getClaimedLandSize(village);
        int maxClaimAllowed = village.getMaxClaimableChunks();
        Set<SimpleChunkLocation> toClaim = new LinkedHashSet<>();
        if (args.length == 1) {
            if (res.hasPermission("kingdoms.command.village.claim.radius")) {
                try {
                    int radius = Integer.parseInt(args[0]);
                    int maxRadius = Config.Settlement.Claiming.MAX_RADIUS;
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
            }
        } else if (args.length >= 2) {
            if (res.hasPermission("kingdoms.command.village.claim.coords")) {
                try {
                    toClaim.add(new SimpleChunkLocation(player.getWorld().getName(), Integer.parseInt(args[0]), Integer.parseInt(args[1])));
                } catch (NumberFormatException e) {
                    getError("not-a-number").tell(res, "input", args[0] + " " + args[1]);
                    return false;
                }
            }
        } else {
            toClaim.add(new SimpleChunkLocation(player.getLocation()));
        }

        if (amountClaimed + toClaim.size() > maxClaimAllowed) {
            res.tell(getError("cant-claim-more"), "max", String.valueOf(maxClaimAllowed));
            return true;
        }

        if (amountClaimed > 0) {
            boolean landAround = false;
            for (SimpleChunkLocation c : toClaim) {
                for (SimpleChunkLocation chunk : WorldUtils.getChunksAdjacent(c)) {
                    OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
                    if (checkLand != null && checkLand.isOwnedBy(village)) {
                        landAround = true;
                        break;
                    }
                }
            }

            if (!landAround) {
                res.tell(getError("no-land-around"));
                return true;
            }
        }

        List<OwnedLand> toSave = new ArrayList<>();
        OUTER:
        for (SimpleChunkLocation c : toClaim) {
            if (amountClaimed >= maxClaimAllowed) {
                res.tell(getError("cant-claim-more"), "max", String.valueOf(maxClaimAllowed));
                return true;
            }
            OwnedLand land = LandManager.getInstance().getLand(c);
            if (land != null) {
                if (toClaim.size() > 1) {
                    continue;
                }
                if (land.getLandOwner().equals(village)) {
                    MessageUtils.tell(player, getError("already-own-land"));
                } else {
                    MessageUtils.tell(player, getError("cant-claim-here"),
                            "owner", land.getLandOwner().getName(),
                            "state", land.getLandOwner().getStateTag()
                    );
                }
                return true;
            }
            for (SimpleChunkLocation chunk : WorldUtils.getChunksAround(c, Config.Settlement.Claiming.ENEMY_DISTANCE)) {
                OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
                if (checkLand == null) {
                    continue;
                }
                if (!checkLand.isOwnedBy(village) && (
                        !checkLand.getLandOwner().isAlly(village) || (
                                !(checkLand.getLandOwner() instanceof Village)
                                        || !((Village) checkLand.getLandOwner()).isAllowAllyClaim()))) {
                    if (toClaim.size() == 1) {
                        res.tell(getError("claim-too-close"),
                                "owner", checkLand.getLandOwner().getName(),
                                "state", checkLand.getLandOwner().getStateTag()
                        );
                        return true;
                    } else {
                        continue OUTER;
                    }
                }
            }
            OwnedLand newLand = LandManager.getInstance().claimLand(c, player.getLocation(), village);
            MessageUtils.tellResidents(village.getOnlineResidents(), getMessage("success"),
                    "player", player.getName(),
                    "world", newLand.getWorldName(),
                    "x", String.valueOf(newLand.getX()),
                    "z", String.valueOf(newLand.getZ()));
            if (newLand.containsHome()) {
                SimpleBlockLocation home = new SimpleBlockLocation(newLand.getLandOwner().getHome());
                getMessage("home-set").tell(village.getOnlineResidents(),
                        "rank", res.getVillageRank().getTag(village),
                        "title", res.getVillageTitle(),
                        "displaytitle", res.getDisplayTitle(),
                        "name", player.getName(),
                        "state", res.getVillage().getState().getTag(),
                        "world", home.getWorld(),
                        "x", String.valueOf(home.getX()),
                        "y", String.valueOf(home.getY()),
                        "z", String.valueOf(home.getZ()));
            }
            toSave.add(newLand);
            amountClaimed++;
        }
        if (!toSave.isEmpty()) {
            if (village.getState() == VillageState.VILLAGE && village.getLevel() == 1) {
                getMessage("new-upkeep").tell(res,
                        "villageupkeep", String.valueOf(village.getUpkeep()),
                        "upkeepperclaim", String.valueOf(village.getClaimUpkeep(player.getWorld().getName()))
                );
            }
            Utils.runAsync(() -> {
                DatabaseManager.getInstance().saveLand(toSave);
                if (args.length >= 3 && "map".equalsIgnoreCase(args[2])) {
                    new ChatMap(res.getPlayer(), toSave.get(0).getMapKey());
                }
            });
        }
        return true;
    }

}
