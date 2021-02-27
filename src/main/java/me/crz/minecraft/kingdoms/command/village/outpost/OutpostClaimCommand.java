package me.crz.minecraft.kingdoms.command.village.outpost;

import me.crz.minecraft.kingdoms.command.village.VillageSubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.utils.config.Config;
import me.crz.minecraft.kingdoms.resident.Resident;
import me.crz.minecraft.kingdoms.village.Village;
import me.crz.minecraft.kingdoms.world.NamedLocation;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;

public class OutpostClaimCommand extends VillageSubCommand {
    public OutpostClaimCommand(VillageSubCommand parent) {
        super(parent, "claim <name>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!Utils.isWorldClaimable(resident.getPlayer().getWorld())) {
            resident.tell(getError("world-not-claimable"), "world", resident.getPlayer().getWorld().getName());
            return true;
        }
        if (args.length < 1) {
            return false;
        }

        if (LandManager.getInstance().getClaimedLand(resident.getVillage()).isEmpty()) {
            getError("no-home").tell(resident);
            return true;
        }

        NamedLocation outpost = resident.getVillage().getOutpostHomes().get(args[0].toLowerCase());
        if (outpost != null) {
            getError("already-exists").tell(resident, "name", outpost.getName());
            return true;
        }

        int amountClaimed = resident.getVillage().getOutpostHomes().size();
        int maxClaimAllowed = resident.getVillage().getMaxOutposts();
        if (amountClaimed >= maxClaimAllowed) {
            resident.tell(getMessage("max-outpost-chunks"), "max", Integer.toString(maxClaimAllowed));
            return true;
        }

        if (!resident.getVillage().has(Config.Settlement.Outpost.CREATION_COST)) {
            getError("not-enough-money").tell(resident, "amount", String.valueOf(Config.Settlement.Outpost.CREATION_COST));
            return true;
        }

        OwnedLand land = LandManager.getInstance().getLand(resident.getPlayer().getLocation());
        if (land != null) {
            if (land.isOwnedBy(resident.getVillage())) {
                resident.tell(getError("already-own-land"));
            } else {
                resident.tell(getError("cant-claim-here"), "owner", land.getLandOwner().getName());
            }
        } else {
            for (SimpleChunkLocation chunk : Utils.getChunksAround(resident.getPlayer(), Config.Settlement.Claiming.ENEMY_DISTANCE)) {
                OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
                if (checkLand == null) {
                    continue;
                }
                if (!checkLand.isOwnedBy(resident.getVillage()) && (
                        !checkLand.getLandOwner().isAlly(resident.getVillage()) || (
                                !(checkLand.getLandOwner() instanceof Village)
                                        || !((Village) checkLand.getLandOwner()).isAllowAllyClaim()))) {
                    resident.tell(getError("claim-too-close"), "owner", checkLand.getLandOwner().getName());
                    return true;
                }
            }
            if (LandManager.getInstance().claimOutpost(resident.getPlayer().getLocation(), resident.getVillage(), args[0]) != null) {
                getMessage("success").tell(resident.getVillage().getOnlineResidents(),
                        "cost", String.valueOf(Config.Settlement.Outpost.CREATION_COST),
                        "player", resident.getName(),
                        "name", args[0],
                        "x", String.valueOf(resident.getPlayer().getLocation().getBlockX()),
                        "z", String.valueOf(resident.getPlayer().getLocation().getBlockZ()));
            } else {
                resident.tell("&cAn error occured while claiming the outpost!");
            }
        }
        return true;
    }
}
