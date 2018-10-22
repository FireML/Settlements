package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.db.DatabaseManager;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VillageClaimCommand extends VillageSubCommand {
    public VillageClaimCommand(SubCommand parent) {
        super(parent, "claim [<radius>]", VillageRank.ELDER);
        setPermission("kingdoms.user");
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
        if (amountClaimed >= maxClaimAllowed) {
            res.tell(getError("cant-claim-more"), "max", String.valueOf(maxClaimAllowed));
            return true;
        }
        List<SimpleChunkLocation> toClaim = new ArrayList<>();
        if (args.length == 1) {
            try {
                int radius = Integer.parseInt(args[0]);
                int maxRadius = KingdomsPlugin.getInstance().getConfig().getInt("claim-max-radius");
                if (radius > maxRadius) {
                    getError("radius-too-large").tell(res, "radius", String.valueOf(radius), "max", String.valueOf(maxRadius));
                    return true;
                }
                toClaim.addAll(Utils.getChunksAround(player, radius));
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(res, "input", args[0]);
                return false;
            }
        } else if (args.length  == 2) {
            try {
                toClaim.add(new SimpleChunkLocation(player.getWorld().getName(), Integer.parseInt(args[0]), Integer.parseInt(args[1])));
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(res, "input", args[0] + " " + args[1]);
                return false;
            }
        } else {
            toClaim.add(new SimpleChunkLocation(player.getLocation()));
        }

        boolean landAround = false;
        for (SimpleChunkLocation c : toClaim) {
            for (SimpleChunkLocation chunk : Utils.getChunksAround(c, 1)) {
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
                if (land.getOwnerVillage().equals(village)) {
                    MessageUtils.tell(player, getError("already-own-land"));
                } else {
                    MessageUtils.tell(player, getError("cant-claim-here"),
                            "owner", land.getOwnerVillage().getName(),
                            "state", land.getOwnerVillage().getState().getTag()
                    );
                }
                return true;
            }
            for (SimpleChunkLocation chunk : Utils.getChunksAround(c, 4)) {
                OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
                if (checkLand == null) {
                    continue;
                }
                if (!checkLand.getOwnerVillage().equals(village) && !checkLand.getOwnerVillage().isAlly(village)) {
                    if (toClaim.size() == 1) {
                        res.tell(getError("claim-too-close"),
                                "owner", checkLand.getOwnerVillage().getName(),
                                "state", checkLand.getOwnerVillage().getState().getTag()
                        );
                        return true;
                    } else {
                        continue OUTER;
                    }
                }
            }
            OwnedLand newLand = LandManager.getInstance().claimLand(player.getLocation(), village);
            MessageUtils.tellResidents(village.getOnlineResidents(), getMessage("success"),
                    "player", player.getName(),
                    "world", newLand.getWorldName(),
                    "x", String.valueOf(newLand.getX()),
                    "z", String.valueOf(newLand.getZ()));
            toSave.add(newLand);
            amountClaimed++;
        }
        if (!toSave.isEmpty()) {
            Utils.runAsync(() -> DatabaseManager.getInstance().saveLand(toSave));
        }
        return true;
    }

}
