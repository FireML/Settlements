package de.themoep.settlements.command.settlement.outpost;

import de.themoep.settlements.command.settlement.SettlementSubCommand;
import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.Message;
import de.themoep.settlements.utils.WorldUtils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.world.NamedLocation;
import de.themoep.settlements.world.SimpleChunkLocation;

public class OutpostClaimCommand extends SettlementSubCommand {
    public OutpostClaimCommand(SettlementSubCommand parent) {
        super(parent, "claim <name>");
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
        if (!resident.getSettlement().canClaim(resident.getPlayer().getWorld())) {
            getError("world-not-claimable").tell(resident, "world", resident.getPlayer().getWorld().getName());
            return true;
        }
        if (args.length < 1) {
            return false;
        }

        if (LandManager.getInstance().getClaimedLand(resident.getSettlement()).isEmpty()) {
            getError("no-home").tell(resident);
            return true;
        }

        NamedLocation outpost = resident.getSettlement().getOutpostHomes().get(args[0].toLowerCase());
        if (outpost != null) {
            getError("already-exists").tell(resident, "name", outpost.getName());
            return true;
        }

        int amountClaimed = resident.getSettlement().getOutpostHomes().size();
        int maxClaimAllowed = resident.getSettlement().getMaxOutposts();
        if (amountClaimed >= maxClaimAllowed) {
            resident.tell(getMessage("max-outpost-chunks"), "max", Integer.toString(maxClaimAllowed));
            return true;
        }

        if (!resident.getSettlement().has(Config.Settlement.Outpost.CREATION_COST)) {
            getError("not-enough-money").tell(resident, "amount", String.valueOf(Config.Settlement.Outpost.CREATION_COST));
            return true;
        }

        OwnedLand land = LandManager.getInstance().getLand(resident.getPlayer().getLocation());
        if (land != null) {
            if (land.isOwnedBy(resident.getSettlement())) {
                if (land.isOutpost()) {
                    getError("already-own-land").tell(resident);
                } else {
                    // Check surrounding land if it is connected to outpost or home
                    if (LandManager.getInstance().isConnected(land)) {
                        getError("already-own-land").tell(resident);
                    } else {
                        // If not connected set outpost at current location
                        land.setOutpost(args[0]);
                        resident.getSettlement().setOutpostHome(args[0], resident.getPlayer().getLocation());
                        resident.getSettlement().save();
                        land.save();
                        Message.get("command.settlement.outpost.sethome.success").tell(resident, "name", args[0]);
                    }
                }
            } else {
                resident.tell(getError("cant-claim-here"), "owner", land.getLandOwner().getName());
            }
        } else {
            for (SimpleChunkLocation chunk : WorldUtils.getChunksAround(resident.getPlayer(), Config.Claiming.ENEMY_DISTANCE)) {
                OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
                if (checkLand == null) {
                    continue;
                }
                if (!checkLand.isOwnedBy(resident.getSettlement()) && (
                        !checkLand.getLandOwner().isAlly(resident.getSettlement()) || (
                                !(checkLand.getLandOwner() instanceof Settlement)
                                        || !checkLand.getLandOwner().isAllowAllyClaim()))) {
                    resident.tell(getError("claim-too-close"), "owner", checkLand.getLandOwner().getName());
                    return true;
                }
            }
            if (LandManager.getInstance().claimOutpost(resident.getPlayer().getLocation(), resident.getSettlement(), args[0]) != null) {
                getMessage("success").tell(resident.getSettlement().getOnlineResidents(),
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
