package me.crz.minecraft.kingdoms.command.village.outpost;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import me.crz.minecraft.kingdoms.world.SimpleChunkLocation;
import org.bukkit.Chunk;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class OutpostUnclaimCommand extends VillageSubCommand {
    public OutpostUnclaimCommand(VillageSubCommand parent) {
        super(parent, "unclaim", VillageRank.CHIEF);
    }

    @Override
    public boolean run(Resident resident, String base, String alias, String... args) {
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

        Collection<OwnedLand> toUnclaim = LandManager.getInstance().getAllConnectedLand(land, search -> {
            if (search.isOutpost()) {
                // return an empty set if this is connected to the home chunk
                getMessage("connected-to-outpost").tell(resident);
                return LandManager.FilterResponse.ABORT;
            } else if (search.getLocation().equals(resident.getVillage().getVillageHome().toChunkLocation())) {
                // return an empty set if this is connected to the home chunk
                getMessage("connected-to-home").tell(resident);
                return LandManager.FilterResponse.ABORT;
            }
            return LandManager.FilterResponse.ACCEPT;
        });

        toUnclaim.add(land);
        LandManager.getInstance().unclaimLand(toUnclaim);
        MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("success"),
                "amount", String.valueOf(toUnclaim.size() - 1),
                "x", String.valueOf(resident.getPlayer().getLocation().getBlockX()),
                "z", String.valueOf(resident.getPlayer().getLocation().getBlockZ()));
        return true;
    }
}
