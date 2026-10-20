package de.themoep.settlements.utils.dialog.claim.states;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.land.OwnedLand;
import de.themoep.settlements.resident.Resident;
import de.themoep.settlements.settlement.Settlement;
import de.themoep.settlements.utils.WorldUtils;
import de.themoep.settlements.utils.config.Config;
import de.themoep.settlements.utils.config.ConfigMessage;
import de.themoep.settlements.utils.dialog.claim.ChunkState;
import de.themoep.settlements.world.SimpleChunkLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class UnclaimedState extends ChunkState {

    public UnclaimedState(@NonNull Player player, @NonNull Chunk chunk) {
        super(player, chunk, NamedTextColor.GRAY);
    }

    @Override
    public boolean click() {
        Settlement settlement = resident.getSettlement();
        if (settlement == null) {
            return false;
        }
        if (!checkIsClaimable(settlement, chunk)) {
            return false;
        }
        if (resident.hasRankPermission("settlement.claim")) {
            LandManager.getInstance().claimLand(
                new SimpleChunkLocation(chunk),
                 chunk.getBlock(8, 0, 8).getLocation().toHighestLocation(),
                resident.getSettlement()
            );
            return true;
        }
        return false;
    }

    private boolean checkIsClaimable(@NonNull Settlement settlement, @NonNull Chunk selected) {
        OwnedLand updatedLand = LandManager.getInstance().getLand(chunk);
        if (updatedLand != null && updatedLand.getSettlement() != null) {
            return false;
        }
        if (settlement.getMaxClaimableChunks() - LandManager.getInstance().getClaimedLandSize(settlement) <= 0) {
            return false;
        }
        if (checkTooClose(settlement, selected)) {
            return false;
        }
        for (SimpleChunkLocation chunk : WorldUtils.getChunksAdjacent(selected)) {
            OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
            if (checkLand != null && checkLand.isOwnedBy(settlement)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkTooClose(Settlement settlement, Chunk selected) {
        for (SimpleChunkLocation chunk : WorldUtils.getChunksAround(new SimpleChunkLocation(selected), Config.Claiming.ENEMY_DISTANCE)) {
            OwnedLand checkLand = LandManager.getInstance().getLand(chunk);
            if (checkLand == null) {
                continue;
            }
            if (!checkLand.isOwnedBy(settlement) &&
                (!checkLand.getSettlement().isAlly(settlement) || (checkLand.getSettlement() == null || !checkLand.getSettlement().isAllowAllyClaim()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NonNull String hoverKey() {
        return "dialog.claiming.chunk-state.unclaimed";
    }

}
