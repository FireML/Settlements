package me.crz.minecraft.kingdoms.command.village.outpost;

import me.crz.minecraft.kingdoms.command.VillageSubCommand;
import me.crz.minecraft.kingdoms.land.LandManager;
import me.crz.minecraft.kingdoms.land.OwnedLand;
import me.crz.minecraft.kingdoms.rank.VillageRank;
import me.crz.minecraft.kingdoms.utils.MessageUtils;
import me.crz.minecraft.kingdoms.village.Resident;
import org.bukkit.Chunk;

import java.util.Collection;

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

        Collection<OwnedLand> toUnclaim = LandManager.getInstance().getAllConnectedLand(land, true);
        toUnclaim.add(land);
        LandManager.getInstance().unclaimLand(toUnclaim);
        MessageUtils.tellResidents(resident.getVillage().getOnlineResidents(), getMessage("success"),
                "amount", String.valueOf(toUnclaim.size() - 1),
                "x", String.valueOf(resident.getPlayer().getLocation().getBlockX()),
                "z", String.valueOf(resident.getPlayer().getLocation().getBlockZ()));
        return true;
    }
}
