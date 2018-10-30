package me.crz.minecraft.kingdoms.command.village.outpost;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.utils.Utils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;

public class OutpostClaimCommand extends VillageSubCommand {
    public OutpostClaimCommand(VillageSubCommand parent) {
        super(parent, "claim <name>", VillageRank.ELDER);
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

        int amountClaimed = resident.getVillage().getOutpostHomes().size();
        int maxClaimAllowed = resident.getVillage().getMaxOutposts();
        if (amountClaimed >= maxClaimAllowed) {
            resident.tell(getMessage("max-outpost-chunks"), "max", Integer.toString(maxClaimAllowed));
            return true;
        }

        if (resident.getVillage().getBank() < Utils.getOutpostCreationCost()) {
            resident.tell(getError("not-enough-money"), "amount", String.valueOf(Utils.getVillageCreationCost()));
            return true;
        }

        OwnedLand land = LandManager.getInstance().getLand(resident.getPlayer().getLocation());
        if (land != null) {
            if (land.isOwnedBy(resident.getVillage())) {
                resident.tell(getError("already-own-land"));
            } else {
                resident.tell(getError("cant-claim-here"), "owner", land.getOwnerVillage().getName());
            }
        } else {
            for (SimpleChunkLocation chunk : Utils.getChunksAround(resident.getPlayer(), Utils.getClaimEnemyDistance())) {
                OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
                if (checkLand == null) {
                    continue;
                }
                if (!checkLand.isOwnedBy(resident.getVillage()) && !checkLand.getOwnerVillage().isAlly(resident.getVillage())) {
                    resident.tell(getError("claim-too-close"), "owner", checkLand.getOwnerVillage().getName());
                    return true;
                }
            }
            if (LandManager.getInstance().claimOutpost(resident.getPlayer().getLocation(), resident.getVillage(), args[0]) != null) {
                MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("success"),
                        "cost", String.valueOf(Utils.getOutpostCreationCost()),
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
