package me.crz.minecraft.kingdoms.command.village;

import me.crz.minecraft.kingdoms.KingdomsPlugin;
import me.crz.minecraft.kingdoms.command.SubCommand;
import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.entity.Player;

import java.util.LinkedHashSet;
import java.util.Set;

public class VillageUnclaimCommand extends VillageSubCommand {
    public VillageUnclaimCommand(SubCommand parent) {
        super(parent, "unclaim [<radius>]", VillageRank.ELDER);
        setPermission("kingdoms.user");
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
        } else if (args.length  == 2) {
            try {
                toUnclaim.add(new SimpleChunkLocation(player.getWorld().getName(), Integer.parseInt(args[0]), Integer.parseInt(args[1])));
            } catch (NumberFormatException e) {
                getError("not-a-number").tell(res, "input", args[0] + " " + args[1]);
                return false;
            }
        } else {
            toUnclaim.add(new SimpleChunkLocation(player.getLocation()));
        }

        if (toUnclaim.isEmpty()) {
            res.tell(getError("no-village"));
            return true;
        }

        for (SimpleChunkLocation c : toUnclaim) {
            OwnedLand land = LandManager.getInstance().getLand(c);
            if (!land.isOwnedBy(res.getVillage())) {
                if (toUnclaim.size() == 1) {
                    res.tell(getError("not-your-village"), "state", land.getOwnerVillage().getState().getTag());
                }
            } else if (land.isOutpost()) {
                if (toUnclaim.size() == 1) {
                    getMessage("outpost").tell(res, "base", base, "outpost", land.getOutpostName());
                }
            } else if (land.containsHome()) {
                if (toUnclaim.size() == 1) {
                    getMessage("home").tell(res, "base", base);
                }
            } else {
                getMessage("success").tell(res.getVillage().getOnlineResidents(),
                        "player", res.getName(),
                        "world", land.getWorldName(),
                        "x", String.valueOf(land.getX() * 16 + 8),
                        "z", String.valueOf(land.getZ() * 16 + 8));
                LandManager.getInstance().unclaimLand(land);
            }
        }
        return true;
    }
}
