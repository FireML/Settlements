package de.themoep.settlements.utils.dialog.claim.states;

import de.themoep.settlements.land.LandManager;
import de.themoep.settlements.utils.dialog.claim.ChunkState;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class ClaimedByYourSettlementState extends ChunkState {

    public ClaimedByYourSettlementState(@NonNull Player player, @NonNull Chunk chunk) {
        super(player, chunk, NamedTextColor.GREEN);
    }

    @Override
    public boolean click() {
        String permission = land.isOutpost() ? "settlement.outpost.unclaim" : "settlement.outpost";
        if (land.getSettlement().equals(resident.getSettlement()) && resident.hasRankPermission(permission)) {
            LandManager.getInstance().unclaimLand(land);
            return true;
        }
        return false;
    }

    @Override
    public @NonNull String hoverKey() {
        return "dialog.claiming.chunk-state.claimed-by-your-settlement";
    }

}
